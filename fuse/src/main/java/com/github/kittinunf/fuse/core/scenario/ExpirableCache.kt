package com.github.kittinunf.fuse.core.scenario

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.fetch.NoFetcher
import com.github.kittinunf.fuse.core.fetch.SimpleFetcher
import com.github.kittinunf.result.Result
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

class ExpirableCache<T : Any>(private val cache: Cache<T>) : Fuse.Cacheable by cache, Fuse.Cacheable.Put<T> by cache {

    @ExperimentalTime
    fun get(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false
    ): Result<T, Exception> = getWithSource(fetcher, timeLimit, useEntryEvenIfExpired).first

    @ExperimentalTime
    fun getWithSource(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false
    ): Pair<Result<T, Exception>, Cache.Source> {
        val key = fetcher.key
        val persistedTimestamp = getTimestamp(key)

        // no timestamp fetch, we need to just fetch the new data
        return if (persistedTimestamp == -1L) {
            put(fetcher) to Cache.Source.ORIGIN
        } else {
            val isExpired = hasExpired(persistedTimestamp, timeLimit)

            // if it is not expired yet, user wants to use it even it is already expired
            if (!isExpired || useEntryEvenIfExpired) {
                cache.getWithSource(fetcher)
            } else {
                // fetch the value from the fetcher and put back if success, if failure we will fallback to the cache
                putOrGetFromCacheIfFailure(fetcher)
            }
        }
    }

    private fun putOrGetFromCacheIfFailure(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Cache.Source> {
        return when (val result = put(fetcher)) {
            is Result.Success -> result to Cache.Source.ORIGIN
            is Result.Failure -> {
                // fallback to cache
                cache.getWithSource(fetcher)
            }
        }
    }

    @ExperimentalTime
    private fun hasExpired(persistedTimestamp: Long, timeLimit: Duration): Boolean {
        val now = System.currentTimeMillis()
        val durationSincePersisted = (now - persistedTimestamp).milliseconds
        return durationSincePersisted > timeLimit
    }
}

// region Value
@ExperimentalTime
fun <T : Any> ExpirableCache<T>.get(
    key: String,
    getValue: (() -> T?)? = null,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Result<T, Exception> {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    return get(fetcher, timeLimit, useEntryEvenIfExpired)
}

@ExperimentalTime
fun <T : Any> ExpirableCache<T>.getWithSource(
    key: String,
    getValue: (() -> T?)? = null,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Pair<Result<T, Exception>, Cache.Source> {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    return getWithSource(fetcher, timeLimit, useEntryEvenIfExpired)
}

@ExperimentalTime
fun <T : Any> ExpirableCache<T>.put(
    key: String,
    putValue: T? = null
): Result<T, Exception> {
    val fetcher = if (putValue == null) NoFetcher<T>(key) else SimpleFetcher(key, { putValue })
    return put(fetcher)
}
// endregion

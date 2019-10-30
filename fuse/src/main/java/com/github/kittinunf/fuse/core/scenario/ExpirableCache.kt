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

@ExperimentalTime
class ExpirableCache<T : Any>(private val cache: Cache<T>) : Fuse.Cacheable by cache,
    Fuse.Cacheable.Put<T> by cache {

    fun get(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false,
        handler: ((Result<T, Exception>) -> Unit)? = null
    ) {
        get(fetcher, timeLimit, useEntryEvenIfExpired) { result, _ -> handler?.invoke(result) }
    }

    fun get(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false,
        handler: ((Result<T, Exception>, Cache.Source) -> Unit)? = null
    ) {
        val key = fetcher.key
        val persistedTimestamp = getTimestamp(key)

        // no timestamp fetch
        if (persistedTimestamp == -1L) {
            put(fetcher) { handler?.invoke(it, Cache.Source.ORIGIN) }
        } else {
            val isExpired = hasExpired(persistedTimestamp, timeLimit)

            // if it is not expired yet, user wants to use it even it is already expired
            if (!isExpired || useEntryEvenIfExpired) {
                cache.get(fetcher, handler)
            } else {
                put(fetcher) { handler?.invoke(it, Cache.Source.ORIGIN) }
            }
        }
    }

    private fun hasExpired(persistedTimestamp: Long, timeLimit: Duration): Boolean {
        val now = System.currentTimeMillis()
        val durationSincePersisted = (now - persistedTimestamp).milliseconds
        return durationSincePersisted > timeLimit
    }
}

@ExperimentalTime
fun <T : Any> ExpirableCache<T>.get(
    key: String,
    getValue: (() -> T?)? = null,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false,
    handler: ((Result<T, Exception>) -> Unit)? = null
) {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    get(fetcher, timeLimit, useEntryEvenIfExpired, handler)
}

@ExperimentalTime
fun <T : Any> ExpirableCache<T>.get(
    key: String,
    getValue: (() -> T?)? = null,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false,
    handler: ((Result<T, Exception>, Cache.Source) -> Unit)? = null
) {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    get(fetcher, timeLimit, useEntryEvenIfExpired, handler)
}

@ExperimentalTime
fun <T : Any> ExpirableCache<T>.put(
    key: String,
    putValue: T? = null,
    handler: ((Result<T, Exception>) -> Unit)? = null
) {
    val fetcher = if (putValue == null) NoFetcher<T>(key) else SimpleFetcher(key, { putValue })
    put(fetcher, handler)
}


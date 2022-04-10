package com.github.kittinunf.fuse.core.scenario

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.fetch.NeverFetcher
import com.github.kittinunf.fuse.core.fetch.SimpleFetcher
import com.github.kittinunf.result.Result
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ExpirableCache<T : Any>(private val cache: Cache<T>) : Cache<T> by cache {

    /**
     *  Get the entry associated with its particular key which provided by the persistence.
     *  This method will automatically fetch if and only if the entry was not already saved in the persistence previously
     *  Otherwise it will return the entry from the persistence.
     *
     *  However, this method concerns the timeLimit specified by the call-site as well, if timeLimit is set, the entry
     *  considered expired if and only if the current - persistedTimestamp > timeLimit. The fetcher will be used to fetch the data from the
     *  the origin again unless the flag useEntryEvenIfExpired is set to true
     *
     * @param fetcher The fetcher object that can be used to fetch the new value from the origin
     * @param timeLimit The time limit that will be considered as expired, default is INFINITE which means that it will never be expired.
     * @param useEntryEvenIfExpired The flag indicates whether we still want to use the entry or not
     * @return Result<T, Exception> The Result that represents the success/failure of the operation
     */
    fun get(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false
    ): Result<T, Exception> = getWithSource(fetcher, timeLimit, useEntryEvenIfExpired).first

    /**
     *  Get the entry associated with its particular key which provided by the persistence and source of the entry
     *  This method will automatically fetch if and only if the entry was not already saved in the persistence previously
     *  Otherwise it will return the entry from the persistence.
     *
     *  However, this method concerns the timeLimit specified by the call-site as well, if timeLimit is set, the entry
     *  considered expired if and only if the current - persistedTimestamp > timeLimit. The fetcher will be used to fetch the data from the
     *  the origin again unless the flag useEntryEvenIfExpired is set to true
     *
     * @param fetcher The fetcher object that can be used to fetch the new value from the origin
     * @param timeLimit The time limit that will be considered as expired, default is INFINITE which means that it will never be expired.
     * @param useEntryEvenIfExpired The flag indicates whether we still want to use the entry or not
     * @return Pair<Result<T, Exception>, Cache.Source> The Pair of the result that represents the success/failure of the operation and The source of the entry
     */
    fun getWithSource(
        fetcher: Fetcher<T>,
        timeLimit: Duration = Duration.INFINITE,
        useEntryEvenIfExpired: Boolean = false
    ): Pair<Result<T, Exception>, Source> {
        val key = fetcher.key
        val persistedTimestamp = getTimestamp(key)

        // no timestamp fetch, we need to just fetch the new data
        return if (persistedTimestamp == null) {
            put(fetcher) to Source.ORIGIN
        } else {
            val isExpired = hasExpired(persistedTimestamp, timeLimit)

            // if it is not expired yet or user wants to use it even it is already expired
            if (!isExpired || useEntryEvenIfExpired) {
                cache.getWithSource(fetcher)
            } else {
                // fetch the value from the fetcher and put back if success, if failure we will fallback to the cache
                putOrGetFromCacheIfFailure(fetcher)
            }
        }
    }

    private fun putOrGetFromCacheIfFailure(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Source> {
        return when (val result = put(fetcher)) {
            is Result.Success -> result to Source.ORIGIN
            is Result.Failure -> {
                // fallback to cache
                cache.getWithSource(fetcher)
            }
        }
    }

    private fun hasExpired(persistedTimestamp: Long, timeLimit: Duration): Boolean {
        val now = System.currentTimeMillis()
        val durationSincePersisted = (now - persistedTimestamp).milliseconds
        return durationSincePersisted > timeLimit
    }
}

// region Value
fun <T : Any> ExpirableCache<T>.get(
    key: String,
    getValue: (() -> T),
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Result<T, Exception> {
    val fetcher = SimpleFetcher(key, getValue)
    return get(fetcher, timeLimit, useEntryEvenIfExpired)
}

fun <T : Any> ExpirableCache<T>.get(
    key: String,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Result<T, Exception> = get(NeverFetcher(key), timeLimit, useEntryEvenIfExpired)

fun <T : Any> ExpirableCache<T>.getWithSource(
    key: String,
    getValue: (() -> T),
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Pair<Result<T, Exception>, Source> {
    val fetcher = SimpleFetcher(key, getValue)
    return getWithSource(fetcher, timeLimit, useEntryEvenIfExpired)
}

fun <T : Any> ExpirableCache<T>.getWithSource(
    key: String,
    timeLimit: Duration = Duration.INFINITE,
    useEntryEvenIfExpired: Boolean = false
): Pair<Result<T, Exception>, Source> = getWithSource(NeverFetcher(key), timeLimit, useEntryEvenIfExpired)

// endregion

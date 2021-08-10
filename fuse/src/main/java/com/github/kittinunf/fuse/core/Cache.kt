package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.cache.Entry
import com.github.kittinunf.fuse.core.fetch.DiskFetcher
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.fetch.NoFetcher
import com.github.kittinunf.fuse.core.fetch.SimpleFetcher
import com.github.kittinunf.fuse.util.md5
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import java.io.File

object CacheBuilder {

    fun <T : Any> config(
        dir: String,
        convertible: Fuse.DataConvertible<T>,
        construct: Config<T>.() -> Unit = {}
    ): Config<T> {
        return Config(dir, convertible = convertible).apply(construct)
    }

    fun <T : Any> config(
        dir: String,
        name: String,
        convertible: Fuse.DataConvertible<T>,
        construct: Config<T>.() -> Unit = {}
    ): Config<T> {
        return Config(dir, name, convertible).apply(construct)
    }
}

fun <T : Any> Config<T>.build(): Cache<T> = CacheImpl(this)

enum class Source {
    ORIGIN,
    MEM,
    DISK,
}

interface Cache<T : Any> :
    Fuse.Cacheable,
    Fuse.Cacheable.Put<T>,
    Fuse.Cacheable.Get<T>,
    Fuse.DataConvertible<T>

class CacheImpl<T : Any> internal constructor(
    private val config: Config<T>
) : Cache<T>, Fuse.DataConvertible<T> by config.convertible {

    private val memCache = config.memCache
    private val diskCache = config.diskCache

    override fun put(fetcher: Fetcher<T>): Result<T, Exception> {
        return fetchAndPut(fetcher)
    }

    override fun get(fetcher: Fetcher<T>): Result<T, Exception> {
        return _get(fetcher).first
    }

    override fun getWithSource(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Source> {
        return _get(fetcher)
    }

    @Suppress("UNCHECKED_CAST")
    private fun _get(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Source> {
        val key = fetcher.key
        val safeKey = key.md5()

        // found in memCache
        memCache.get(safeKey)?.let { value ->
            // move specific key in disk cache up as it is found in mem
            val result = Result.of<T, Exception> {
                if (diskCache.get(safeKey) == null) {
                    val converted = convertToData(value as T)
                    // we found this in memCache, so we need to retrieve timeStamp that was saved in memCache back to diskCache
                    val timeWasPersisted = memCache.getTimestamp(safeKey)
                    diskCache.put(safeKey, Entry(key, converted, timeWasPersisted ?: -1))
                }
                value as T
            }
            return result to Source.MEM
        }

        // find in diskCache
        val bytes = diskCache.get(safeKey)
        if (bytes == null) {
            // not found we need to fetch then put it back
            return fetchAndPut(fetcher) to Source.ORIGIN
        } else {
            // found in disk, save back into mem
            val result = Result.of<T, Exception> {
                // we found this in disk cache, so we need to retrieve timeStamp that was stored in diskCache back to memCache
                val converted = convertFromData(bytes)

                val timeWasPersisted = diskCache.getTimestamp(safeKey)
                // put the converted version into the memCache
                memCache.put(safeKey, Entry(key, converted, timeWasPersisted ?: -1))
                converted
            }
            return result to Source.DISK
        }
    }

    private fun put(key: String, value: T): Result<T, Exception> {
        val transformed = config.transformer(key, value)

        // save the persist timing
        val timeToPersist = System.currentTimeMillis()
        val safeKey = key.md5()

        memCache.put(safeKey, Entry(key, transformed, timeToPersist))
        return Result.of {
            val converted = convertToData(transformed)
            diskCache.put(safeKey, Entry(key, converted, timeToPersist))
            transformed
        }
    }

    override fun remove(key: String, fromSource: Source): Boolean {
        require(fromSource != Source.ORIGIN) { "Cannot remove from Source.ORIGIN" }

        val safeKey = key.md5()
        return when (fromSource) {
            Source.MEM -> memCache.remove(safeKey)
            Source.DISK -> diskCache.remove(safeKey)
            else -> {
                false
            }
        }
    }

    override fun removeAll() {
        memCache.removeAll()
        diskCache.removeAll()
    }

    override fun allKeys(): Set<String> {
        val keys = memCache.allKeys()
        return keys.takeIf { it.isNotEmpty() } ?: diskCache.allKeys()
    }

    override fun hasKey(key: String): Boolean {
        val safeKey = key.md5()
        val value = memCache.get(safeKey) ?: diskCache.get(safeKey)
        return value != null
    }

    override fun getTimestamp(key: String): Long {
        val safeKey = key.md5()
        return memCache.getTimestamp(safeKey) ?: diskCache.getTimestamp(safeKey) ?: -1
    }

    private fun fetchAndPut(fetcher: Fetcher<T>): Result<T, Exception> {
        val fetchResult = fetcher.fetch()
        return fetchResult.flatMap { put(fetcher.key, it) }
    }
}

// region File
fun <T : Any> Cache<T>.get(file: File): Result<T, Exception> = get(DiskFetcher(file, this))

fun <T : Any> Cache<T>.getWithSource(file: File): Pair<Result<T, Exception>, Source> =
    getWithSource(DiskFetcher(file, this))

fun <T : Any> Cache<T>.put(file: File): Result<T, Exception> = put(DiskFetcher(file, this))
// endregion File

// region Value
fun <T : Any> Cache<T>.get(key: String, getValue: (() -> T?)? = null): Result<T, Exception> {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    return get(fetcher)
}

fun <T : Any> Cache<T>.getWithSource(
    key: String,
    getValue: (() -> T?)? = null
): Pair<Result<T, Exception>, Source> {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    return getWithSource(fetcher)
}

fun <T : Any> Cache<T>.put(key: String, putValue: T? = null): Result<T, Exception> {
    val fetcher = if (putValue == null) NoFetcher<T>(key) else SimpleFetcher(key, { putValue })
    return put(fetcher)
}
// endregion Value

package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.cache.DiskCache
import com.github.kittinunf.fuse.core.cache.MemCache
import com.github.kittinunf.fuse.core.cache.Persistence
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.md5
import com.github.kittinunf.fuse.util.thread
import com.github.kittinunf.result.Result

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
        construct: Config<T>.() -> Unit
    ): Config<T> {
        return Config(dir, name, convertible).apply(construct)
    }
}

fun <T : Any> Config<T>.build() = Cache(this)

class Cache<T : Any> internal constructor(private val config: Config<T>) : Fuse.Cacheable<T>,
    Fuse.DataConvertible<T> by config.convertible {

    enum class Source {
        NOT_FOUND,
        MEM,
        DISK,
    }

    private val memCache: Persistence<Any> by lazy { MemCache() }
    private val diskCache: Persistence<ByteArray> by lazy {
        DiskCache.open(
            config.cacheDir,
            config.name,
            config.diskCapacity
        )
    }

    override fun put(fetcher: Fetcher<T>, success: ((Result<T, Exception>) -> Unit)?) {
        dispatch(config.dispatchedExecutor) {
            fetchAndPut(fetcher) { result ->
                thread(config.callbackExecutor) {
                    success?.invoke(result)
                }
            }
        }
    }

    override fun get(fetcher: Fetcher<T>, handler: ((Result<T, Exception>) -> Unit)?) {
        get(fetcher, handler, handler, handler)
    }

    override fun get(fetcher: Fetcher<T>, handler: ((Result<T, Exception>, Source) -> Unit)?) {
        get(fetcher,
            { handler?.invoke(it, Source.MEM) },
            { handler?.invoke(it, Source.DISK) },
            { handler?.invoke(it, Source.NOT_FOUND) })
    }

    private fun get(
        fetcher: Fetcher<T>,
        memHandler: ((Result<T, Exception>) -> Unit)?,
        diskHandler: ((Result<T, Exception>) -> Unit)?,
        fetchHandler: ((Result<T, Exception>) -> Unit)?
    ) {

        val key = fetcher.key
        val safeKey = key.md5()

        // found in memCache
        memCache.get(safeKey)?.let { value ->
            dispatch(config.dispatchedExecutor) {
                // move specific key in disk cache up as it is found in mem
                val result = Result.of<T, Exception> {
                    val converted = convertToData(value as T)
                    if (diskCache.get(safeKey) == null) {
                        // we found this in memCache, so we need to retrieve timeStamp that was saved in memCache back to diskCache
                        val timeWasPersisted = memCache.getTimestamp(safeKey)
                        diskCache.put(safeKey, key, converted, timeWasPersisted ?: -1)
                    }
                    value
                }
                thread(config.callbackExecutor) {
                    memHandler?.invoke(result)
                }
            }
            return
        }

        dispatch(config.dispatchedExecutor) {
            // find in diskCache
            val bytes = diskCache.get(safeKey)
            if (bytes == null) {
                // not found we need to fetch then put it back
                fetchAndPut(fetcher) { result ->
                    thread(config.callbackExecutor) {
                        fetchHandler?.invoke(result)
                    }
                }
            } else {
                // found in disk, save back into mem
                val result = Result.of<T, Exception> {
                    val converted = convertFromData(bytes)
                    // we found this in disk cache, so we need to retrieve timeStamp that was stored in diskCache back to memCache
                    val timeWasPersisted = diskCache.getTimestamp(safeKey)
                    memCache.put(safeKey, key, bytes, timeWasPersisted ?: -1)
                    converted
                }
                thread(config.callbackExecutor) {
                    diskHandler?.invoke(result)
                }
            }
        }
    }

    private fun put(key: String, value: T, success: ((Result<T, Exception>) -> Unit)? = null) {
        dispatch(config.dispatchedExecutor) {
            // save the persist timing
            val timeToPersist = System.currentTimeMillis()

            applyTransformer(key, value) { transformed ->
                val safeKey = key.md5()
                memCache.put(safeKey, key, transformed, timeToPersist)
                val result = Result.of<T, Exception> {
                    val converted = convertToData(transformed)
                    diskCache.put(safeKey, key, converted, timeToPersist)
                    transformed
                }
                thread(config.callbackExecutor) {
                    success?.invoke(result)
                }
            }
        }
    }

    override fun remove(key: String, removeOnlyInMemory: Boolean) {
        val safeKey = key.md5()
        memCache.remove(safeKey)
        if (!removeOnlyInMemory) diskCache.remove(safeKey)
    }

    override fun removeAll(removeOnlyInMemory: Boolean) {
        memCache.removeAll()
        if (!removeOnlyInMemory) diskCache.removeAll()
    }

    override fun allKeys(): Set<String> {
        val keys = memCache.allKeys()
        return keys.takeIf { it.isNotEmpty() } ?: diskCache.allKeys()
    }

    private fun fetchAndPut(
        fetcher: Fetcher<T>,
        handler: ((Result<T, Exception>) -> Unit)? = null
    ) {
        fetcher.fetch { result ->
            result.fold({ value ->
                put(fetcher.key, value, handler)
            }, { exception ->
                handler?.invoke(Result.error(exception))
            })
        }
    }

    private fun applyTransformer(key: String, value: T, success: (T) -> Unit) {
        val transformed = config.transformer(key, value)
        success(transformed)
    }
}

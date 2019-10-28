package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.cache.DiskCache
import com.github.kittinunf.fuse.core.cache.MemCache
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.md5
import com.github.kittinunf.fuse.util.thread
import com.github.kittinunf.result.Result

object CacheBuilder {

    fun <T : Any> config(dir: String, construct: Config<T>.() -> Unit = {}): Config<T> {
        return Config<T>(dir).apply(construct)
    }

    fun <T : Any> config(dir: String, name: String, construct: Config<T>.() -> Unit): Config<T> {
        return Config<T>(dir, name).apply(construct)
    }
}

fun <T : Any> Config<T>.build(convertible: Fuse.DataConvertible<T>) = Cache(this, convertible)

class Cache<T : Any> internal constructor(
    private val config: Config<T>,
    convertible: Fuse.DataConvertible<T>
) : Fuse.DataConvertible<T> by convertible {

    enum class Source {
        NOT_FOUND,
        MEM,
        DISK,
    }

    private val memCache by lazy { MemCache() }

    private val diskCache by lazy {
        DiskCache.open(
            config.cacheDir,
            config.name,
            config.diskCapacity
        )
    }

    fun put(fetcher: Fetcher<T>, success: ((Result<T, Exception>) -> Unit)? = null) {
        dispatch(config.dispatchedExecutor) {
            fetchAndPut(fetcher) { result ->
                thread(config.callbackExecutor) {
                    success?.invoke(result)
                }
            }
        }
    }

    fun put(key: String, value: T, success: ((Result<T, Exception>) -> Unit)? = null) {
        dispatch(config.dispatchedExecutor) {
            applyTransformer(key, value) { transformed ->
                val safeKey = key.md5()
                memCache.put(safeKey, key, transformed)
                val result = Result.of<T, Exception> {
                    val converted = convertToData(transformed)
                    diskCache.put(safeKey, key, converted)
                    transformed
                }
                thread(config.callbackExecutor) {
                    success?.invoke(result)
                }
            }
        }
    }

    fun get(fetcher: Fetcher<T>, handler: ((Result<T, Exception>) -> Unit)? = null) {
        _get(fetcher, handler, handler, handler)
    }

    fun get(fetcher: Fetcher<T>, handler: ((Result<T, Exception>, Source) -> Unit)? = null) {
        _get(fetcher,
            { handler?.invoke(it, Source.MEM) },
            { handler?.invoke(it, Source.DISK) },
            { handler?.invoke(it, Source.NOT_FOUND) })
    }

    private fun _get(
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
                    diskCache.setIfMissing(safeKey, key, converted)
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
                fetchAndPut(fetcher, fetchHandler)
            } else {
                // found in disk, save back into mem
                val result = Result.of<T, Exception> {
                    val converted = convertFromData(bytes)
                    memCache.put(safeKey, key, bytes)
                    converted
                }
                thread(config.callbackExecutor) {
                    diskHandler?.invoke(result)
                }
            }
        }
    }

    fun remove(key: String, removeOnlyInMemory: Boolean = false) {
        val safeKey = key.md5()
        memCache.remove(safeKey)
        if (!removeOnlyInMemory) diskCache.remove(safeKey)
    }

    fun removeAll(removeOnlyInMemory: Boolean = false) {
        memCache.removeAll()
        if (!removeOnlyInMemory) diskCache.removeAll()
    }

    fun allKeys(): Set<String> {
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

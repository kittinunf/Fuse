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

    fun put(
        key: String,
        value: T,
        handler: ((Result<T, Exception>) -> Unit)? = null
    ) {
        handler?.invoke(Result.of {
            _put(key, value)
            value
        })
    }

    private fun _put(
        key: String,
        value: T,
        success: ((T) -> Unit)? = null
    ) {
        dispatch(config.dispatchedExecutor) {
            apply(value, config) { transformed ->
                val hashed = key.md5()
                memCache.put(hashed, transformed)
                diskCache.put(hashed, convert(transformed, config))
                thread(config.callbackExecutor) {
                    success?.invoke(transformed)
                }
            }
        }
    }

    fun get(
        fetcher: Fetcher<T>,
        handler: ((Result<T, Exception>) -> Unit)? = null
    ) {
        _get(fetcher, handler, handler, handler, { handler?.invoke(Result.error(it)) })
    }

    fun get(
        fetcher: Fetcher<T>,
        handler: ((Result<T, Exception>, Source) -> Unit)? = null
    ) {
        _get(fetcher,
            { handler?.invoke(it, Source.MEM) },
            { handler?.invoke(it, Source.DISK) },
            { handler?.invoke(it, Source.NOT_FOUND) },
            { handler?.invoke(Result.error(it), Source.NOT_FOUND) })
    }

    private fun _get(
        fetcher: Fetcher<T>,
        memHandler: ((Result<T, Exception>) -> Unit)?,
        diskHandler: ((Result<T, Exception>) -> Unit)?,
        fetchHandler: ((Result<T, Exception>) -> Unit)?,
        errorHandler: (Exception) -> Unit
    ) {

        val key = fetcher.key
        val hashed = key.md5()

        // find in memCache
        memCache.get(hashed)?.let { value ->
            dispatch(config.dispatchedExecutor) {
                // move specific key in disk cache up as it is found in mem
                diskCache.setIfMissing(hashed, convertToData(value as T))
                thread(config.callbackExecutor) {
                    memHandler?.invoke(Result.of(value))
                }
            }
            return
        }

        dispatch(config.dispatchedExecutor) {
            // find in diskCache
            val bytes = diskCache.get(hashed)
            val value = bytes?.let { convertFromData(bytes) }
            if (value == null) {
                // not found we need to fetch then put it back
                fetchAndPut(fetcher, fetchHandler)
            } else {
                // found in disk, save into mem
                memCache.put(hashed, value)
                thread(config.callbackExecutor) {
                    diskHandler?.invoke(Result.of(value))
                }
            }
        }
    }

    fun remove(
        key: String,
        removeOnlyInMemory: Boolean = false
    ) {
        val hashed = key.md5()
        memCache.remove(hashed)
        if (!removeOnlyInMemory) diskCache.remove(hashed)
    }

    private fun convert(value: T, config: Config<T>): ByteArray {
        val converter = config.convertToData ?: { convertToData(value) }
        return converter(value)
    }

    private fun fetchAndPut(
        fetcher: Fetcher<T>,
        handler: ((Result<T, Exception>) -> Unit)? = null
    ) {
        fetcher.fetch { result ->
            result.fold({ value ->
                _put(fetcher.key, value) {
                    handler?.invoke(Result.of(it))
                }
            }, { exception ->
                handler?.invoke(Result.error(exception))
            })
        }
    }

    private fun apply(value: T, config: Config<T>, success: (T) -> Unit) {
        val transformed = config.transformer(value)
        success(transformed)
    }
}

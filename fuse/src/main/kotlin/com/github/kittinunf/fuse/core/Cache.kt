package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.cache.DiskCache
import com.github.kittinunf.fuse.core.cache.MemCache
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.let
import com.github.kittinunf.fuse.util.mainThread
import com.github.kittinunf.fuse.util.md5
import com.github.kittinunf.result.Result
import java.util.concurrent.Executors

open class Cache<T : Any>(cacheDir: String,
                          convertible: Fuse.DataConvertible<T>,
                          representable: Fuse.DataRepresentable<T>) : Fuse.DataConvertible<T> by convertible, Fuse.DataRepresentable<T> by representable {

    enum class Type {
        NOT_FOUND,
        MEM,
        DISK,
    }

    private val configs = hashMapOf<String, Triple<Config<T>, MemCache, DiskCache>>()

    private val backgroundExecutor by lazy { Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors()) }

    init {
        val defaultConfig = Config<T>(cacheDir)
        addConfig(defaultConfig)
    }

    fun addConfig(config: Config<T>) {
        val name = config.name
        val memCache = MemCache()
        val diskCache = DiskCache.open(config.cacheDir, name, config.diskCapacity)
        val value = Triple(config, memCache, diskCache)
        configs += (name to value)
    }

    fun put(key: String, value: T, configName: String = Config.DEFAULT_NAME, success: ((T) -> Unit)? = null) {
        configs[configName]?.let { config, memCache, diskCache ->
            applyConfig(value, config) { transformed ->
                val hashed = key.md5()
                dispatch(backgroundExecutor) {
                    memCache[hashed] = transformed
                    diskCache[hashed] = convert(transformed, config)
                    mainThread {
                        success?.invoke(transformed)
                    }
                }

            }
        } ?: throw RuntimeException("Unable to find preset config, you must add config before putting data")
    }

    fun get(fetcher: Fetcher<T>, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>) -> Unit)? = null) {
        _get(fetcher, configName, handler, handler, handler, { handler?.invoke(Result.error(it)) })
    }

    fun get(fetcher: Fetcher<T>, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>, Type) -> Unit)? = null) {
        _get(fetcher,
                configName,
                { handler?.invoke(it, Type.MEM) },
                { handler?.invoke(it, Type.DISK) },
                { handler?.invoke(it, Type.NOT_FOUND) },
                { handler?.invoke(Result.error(it), Type.NOT_FOUND) })
    }

    private fun _get(fetcher: Fetcher<T>, configName: String = Config.DEFAULT_NAME,
                     memHandler: ((Result<T, Exception>) -> Unit)?,
                     diskHandler: ((Result<T, Exception>) -> Unit)?,
                     fetchHandler: ((Result<T, Exception>) -> Unit)?,
                     errorHandler: (Exception) -> Unit) {

        val key = fetcher.key
        val hashed = key.md5()

        configs[configName]?.let { config, memCache, diskCache ->
            //find in memCache
            memCache[hashed]?.let { value ->
                dispatch(backgroundExecutor) {
                    val t = value as T
                    //move specific key in disk cache up as it is found in mem
                    diskCache.setIfMissing(hashed, convertToData(t))
                    mainThread {
                        memHandler?.invoke(Result.of(t))
                    }
                }
                return
            }

            dispatch(backgroundExecutor) {
                //find in diskCache
                val bytes = diskCache[hashed]
                val value = bytes?.let { convertFromData(bytes) }
                if (value == null) {
                    //not found we need to fetch then put it back
                    fetchAndPut(fetcher, config, fetchHandler)
                } else {
                    //found in disk, save into mem
                    memCache[hashed] = value
                    mainThread {
                        diskHandler?.invoke(Result.of(value))
                    }
                }
            }
        } ?: errorHandler(RuntimeException("Config $configName is not found"))
    }

    fun remove(key: String, configName: String = Config.DEFAULT_NAME) {
        configs[configName]?.let { config, memCache, diskCache ->
            memCache.remove(key)
            diskCache.remove(key)
        }
    }

    private fun convert(value: T, config: Config<T>): ByteArray {
        val converter = config.convertToData ?: { convertToData(value) }
        return converter(value)
    }

    private fun fetchAndPut(fetcher: Fetcher<T>, config: Config<T>, handler: ((Result<T, Exception>) -> Unit)? = null) {
        fetcher.fetch { result ->
            result.fold({ value ->
                put(fetcher.key, value, config.name) {
                    handler?.invoke(Result.of(it))
                }
            }, { exception ->
                handler?.invoke(Result.error(exception))
            })
        }
    }

    private fun applyConfig(value: T, config: Config<T>, success: (T) -> Unit) {
        dispatch(backgroundExecutor) {
            val transformed = config.transformer(value)
            mainThread {
                success(transformed)
            }
        }
    }

}

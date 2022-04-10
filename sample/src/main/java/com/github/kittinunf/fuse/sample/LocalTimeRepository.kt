package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.result.Result
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.time.Duration

interface LocalTimeRepository {

    fun getFromNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    fun getFromCache(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    // cache + network
    fun getFromCacheThenNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    fun getFromCacheIfNotExpired(location: String, duration: Duration, handler: (Result<LocalTime, Exception>, Source) -> Unit)

    fun evictCache()
}

class LocalTimeRepositoryImpl(private val network: LocalTimeService, private val cache: CacheableLocalTimeService) : LocalTimeRepository {

    override fun getFromNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        dispatchDefault {
            val result = network.getTime(location)

            mainThread {
                handler(result)
            }
        }
    }

    override fun getFromCache(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        dispatchDefault {
            val result = cache.getLocalTimeOnlyCache(location)

            mainThread {
                handler(result)
            }
        }
    }

    override fun getFromCacheThenNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        dispatchDefault {
            val r1 = cache.getTime(location)

            mainThread {
                handler(r1)
            }
        }

        dispatchDefault {
            Thread.sleep(500) // deliberately make it slower
            val r2 = network.getTime(location)

            mainThread {
                handler(r2)
            }
        }
    }

    override fun getFromCacheIfNotExpired(location: String, duration: Duration, handler: (Result<LocalTime, Exception>, Source) -> Unit) {
        dispatchDefault {
            val (result, source) = cache.getLocalTimeIfNotExpired(location, duration)

            mainThread {
                handler(result, source)
            }
        }
    }

    override fun evictCache() {
        cache.evictCache()
    }
}

private fun <T> T.dispatchDefault(block: FuseAsync<T>.() -> Unit): Future<Unit> =
    dispatch(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()), block)

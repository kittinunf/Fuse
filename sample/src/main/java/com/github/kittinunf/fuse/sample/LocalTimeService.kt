package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.result.Result
import java.util.concurrent.Executors
import java.util.concurrent.Future

interface LocalTimeService {

    fun getFromNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    fun getFromCache(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    // cache + network
    fun getFromBoth(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    // get from cache if available within 5 minutes otherwise refresh from network
    fun getFromCacheIfNotExpired(location: String, handler: (Result<LocalTime, Exception>, Source) -> Unit)
}

class LocalTimeServiceImpl(private val network: LocalTimeRepository, private val cache: CacheableLocalTimeRepository) :
    LocalTimeService {

    override fun getFromNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        dispatchDefault {
            val result = network.getLocalTime(location)

            mainThread {
                handler(result)
            }
        }
    }

    override fun getFromCache(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        dispatchDefault {
            val result = cache.getLocalTime(location)

            mainThread {
                handler(result)
            }
        }
    }

    override fun getFromBoth(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        // this relies on the fact that network will always be slower than cache, in the real world usage, this probably a bad idea ...

        dispatchDefault {
            val r1 = cache.getLocalTime(location)

            mainThread {
                handler(r1)
            }
        }

        dispatchDefault {
            val r2 = network.getLocalTime(location)

            mainThread {
                handler(r2)
            }
        }
    }

    override fun getFromCacheIfNotExpired(
        location: String,
        handler: (Result<LocalTime, Exception>, Source) -> Unit
    ) {
        dispatchDefault {
            val (result, source) = cache.getLocalTimeIfNotExpired(location)

            mainThread {
                handler(result, source)
            }
        }
    }
}

private val executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors())
private fun <T> T.dispatchDefault(block: FuseAsync<T>.() -> Unit): Future<Unit> = dispatch(executor, block)

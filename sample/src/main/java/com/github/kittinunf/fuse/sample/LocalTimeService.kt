package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.result.Result

interface LocalTimeService {

    fun getFromNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    fun getFromCache(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    // cache + network
    fun getFromBoth(location: String, handler: (Result<LocalTime, Exception>) -> Unit)

    // get from cache if available within 5 minutes otherwise refresh from network
    fun getFromCacheIfNotExpired(location: String, handler: (Result<LocalTime, Exception>, Cache.Source) -> Unit)
}

class LocalTimeServiceImpl(private val network: LocalTimeRepository, private val cache: CacheableLocalTimeRepository) :
    LocalTimeService {

    override fun getFromNetwork(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        network.getLocalTime(location.continent, location.area, handler)
    }

    override fun getFromCache(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        cache.getLocalTime(location.continent, location.area, handler)
    }

    override fun getFromBoth(location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        // this relies on the fact that network will always be slower than cache, in the real world usage, this probably a bad idea ...
        cache.getLocalTime(location.continent, location.area, handler)
        network.getLocalTime(location.continent, location.area, handler)
    }

    override fun getFromCacheIfNotExpired(
        location: String,
        handler: (Result<LocalTime, Exception>, Cache.Source) -> Unit
    ) {
        cache.getLocalTimeIfNotExpired(location.continent, location.area, handler)
    }
}

// :)
private val String.continent: String
    get() = substringBefore("/")

private val String.area: String
    get() = substringAfter("/")

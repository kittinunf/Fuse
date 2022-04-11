package com.github.kittinunf.fuse.sample

import android.content.Context
import com.github.kittinunf.fuse.android.config
import com.github.kittinunf.fuse.android.defaultAndroidMemoryCache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.get
import com.github.kittinunf.fuse.core.scenario.ExpirableCache
import com.github.kittinunf.result.Result
import java.net.URL
import kotlin.time.Duration

interface LocalTimeService {

    fun getTime(place: String): Result<LocalTime, Exception>
}

interface CacheableLocalTimeService : LocalTimeService {

    fun getLocalTimeOnlyCache(place: String): Result<LocalTime, Exception>

    fun getLocalTimeIfNotExpired(place: String, duration: Duration): Pair<Result<LocalTime, Exception>, Source>

    fun evictCache()
}

class NetworkService : LocalTimeService {

    override fun getTime(place: String): Result<LocalTime, Exception> {
        val area = place.continent
        val location = place.area
        val fetcher = NetworkFetcher(URL("http://worldtimeapi.org/api/timezone/$area/$location"), LocalTime.deserializer)
        return fetcher.fetch()
    }
}

// in real-world application you should not do this, you should do some injection to construct this or something else, but this is a sample application so ¯\_(ツ)_/¯
class CacheService(context: Context) : CacheableLocalTimeService {

    private val cache = CacheBuilder.config(context, convertible = LocalTime.deserializer) {
        memCache = defaultAndroidMemoryCache()
    }.build()

    private val expirableCache = ExpirableCache(cache)

    override fun getLocalTimeOnlyCache(place: String): Result<LocalTime, Exception> {
        val key = getURL(place).toString()
        // fetch from cache only
        return cache.get(key)
    }

    override fun getTime(place: String): Result<LocalTime, Exception> {
        val fetcher = NetworkFetcher(getURL(place), LocalTime.deserializer)
        // fetch from cache if there, if not then use NetworkFetcher to get new content
        return cache.get(fetcher)
    }

    override fun getLocalTimeIfNotExpired(place: String, duration: Duration): Pair<Result<LocalTime, Exception>, Source> {
        val fetcher = NetworkFetcher(getURL(place), LocalTime.deserializer)
        // fetch from cache with expiration (in duration) if there, if not then use NetworkFetcher to get new content
        return expirableCache.getWithSource(fetcher, timeLimit = duration)
    }

    override fun evictCache() {
        cache.removeAll()
    }

    private fun getURL(place: String): URL {
        val area = place.continent
        val location = place.area
        return URL("http://worldtimeapi.org/api/timezone/$area/$location")
    }
}

private val String.continent: String
    get() = substringBefore("/")

private val String.area: String
    get() = substringAfter("/")

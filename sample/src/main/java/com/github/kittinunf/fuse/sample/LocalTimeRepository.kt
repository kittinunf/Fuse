package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuse.android.defaultAndroidMemoryCache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.scenario.ExpirableCache
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

interface LocalTimeRepository {

    fun getLocalTime(place: String): Result<LocalTime, Exception>
}

interface CacheableLocalTimeRepository : LocalTimeRepository {

    fun getLocalTimeIfNotExpired(place: String): Pair<Result<LocalTime, Exception>, Source>
}

private val fuel = FuelManager().apply {
    basePath = "http://worldtimeapi.org/api/"
}

class NetworkRepository : LocalTimeRepository {

    override fun getLocalTime(place: String): Result<LocalTime, Exception> {
        val area = place.continent
        val location = place.area
        return fuel.get("/timezone/$area/$location").responseObject(LocalTime.deserializer).third
    }
}

class CacheRepository(dir: String) : CacheableLocalTimeRepository {

    private val cache = CacheBuilder.config(dir = dir, name = "SAMPLE", convertible = StringDataConvertible()) {
        memCache = defaultAndroidMemoryCache()
    }.build()

    private val expirableCache = ExpirableCache(cache)

    override fun getLocalTime(place: String): Result<LocalTime, Exception> {
        val area = place.continent
        val location = place.area
        return cache.get(TimeFetcher(area, location)).map { LocalTime.fromJson(it) }
    }

    @OptIn(ExperimentalTime::class)
    override fun getLocalTimeIfNotExpired(place: String): Pair<Result<LocalTime, Exception>, Source> {
        val area = place.continent
        val location = place.area

        val result = expirableCache.getWithSource(TimeFetcher(area, location), timeLimit = Duration.minutes(5))
        return (result.first.map { LocalTime.fromJson(it) } to result.second)
    }

    private class TimeFetcher(private val area: String, private val location: String) : Fetcher<String> {
        override val key: String = LocalTime::class.java.name

        override fun fetch(): Result<String, Exception> = fuel.get("/timezone/$area/$location").responseString().third
    }
}

// :)
private val String.continent: String
    get() = substringBefore("/")

private val String.area: String
    get() = substringAfter("/")

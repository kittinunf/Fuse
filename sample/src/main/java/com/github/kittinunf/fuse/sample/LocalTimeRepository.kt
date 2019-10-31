package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.scenario.ExpirableCache
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

interface LocalTimeRepository {

    fun getLocalTime(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit)
}

interface CacheableLocalTimeRepository : LocalTimeRepository {

    fun getLocalTimeIfNotExpired(
        area: String,
        location: String,
        handler: (Result<LocalTime, Exception>, Cache.Source) -> Unit
    )
}

private val fuel = FuelManager().apply {
    basePath = "http://worldtimeapi.org/api/"
}

class NetworkRepository : LocalTimeRepository {

    override fun getLocalTime(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        fuel.get("/timezone/$area/$location").responseObject(LocalTime.deserializer, handler)
    }
}

class CacheRepository(dir: String) : CacheableLocalTimeRepository {

    private val cache = CacheBuilder.config(dir = dir, name = "SAMPLE", convertible = StringDataConvertible()).build()
    private val expirableCache = ExpirableCache(cache)

    override fun getLocalTime(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        cache.get(TimeFetcher(area, location)) { result, _ ->
            handler(result.map { LocalTime.fromJson(it) })
        }
    }

    @ExperimentalTime
    override fun getLocalTimeIfNotExpired(
        area: String,
        location: String,
        handler: (Result<LocalTime, Exception>, Cache.Source) -> Unit
    ) {
        expirableCache.get(TimeFetcher(area, location), timeLimit = 5.minutes) { result, source ->
            handler(result.map { LocalTime.fromJson(it) }, source)
        }
    }

    private class TimeFetcher(private val area: String, private val location: String) : Fetcher<String> {
        override val key: String = LocalTime::class.java.name

        override fun fetch(): Result<String, Exception> = fuel.get("/timezone/$area/$location").responseString().third
    }
}

package com.github.kittinunf.fuse.sample

import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map

interface TimeRepository {

    fun getLocalTime(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit)
}

private val manager = FuelManager().apply {
    basePath = "http://worldtimeapi.org/api/"
}

class NetworkRepository(private val fuel: FuelManager = manager) : TimeRepository {

    override fun getLocalTime(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        fuel.get("/timezone/$area/$location").responseObject(LocalTime.deserializer, handler)
    }
}

class CacheRepository(dir: String, private val fuel: FuelManager = manager) : TimeRepository {

    private val cache = CacheBuilder.config(dir = dir, name = "SAMPLE", convertible = StringDataConvertible()).build()

    override fun getLocalTime(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        val fetcher = object : Fetcher<String> {
            override val key: String = LocalTime::class.java.name

            override fun fetch(handler: (Result<String, Exception>) -> Unit) {
                fuel.get("/timezone/$area/$location").responseString(handler)
            }
        }

        cache.get(fetcher) { result, _ ->
            handler(result.map { LocalTime.fromJson(it) })
        }
    }
}
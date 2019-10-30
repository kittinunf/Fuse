package com.github.kittinunf.fuse.sample

import com.github.kittinunf.result.Result

interface TimeService {

    fun getFromNetwork(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit)
    fun getFromCache(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit)
}

class TimeServiceImpl(private val network: TimeRepository, private val cache: TimeRepository) : TimeService {

    override fun getFromNetwork(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        network.getLocalTime(area, location, handler)
    }

    override fun getFromCache(area: String, location: String, handler: (Result<LocalTime, Exception>) -> Unit) {
        cache.getLocalTime(area, location, handler)
    }
}
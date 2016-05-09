package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.result.Result

interface Fetcher<T : Any> {

    val key: String

    fun fetch(handler: (Result<T, Exception>) -> Unit)

    fun cancel() {
    }

}

class SimpleFetcher<T : Any>(override val key: String, val getValue: () -> T?) : Fetcher<T> {

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        handler(Result.of(getValue(), { KotlinNullPointerException() }))
    }

}

@JvmName("get")
fun <T : Any> Cache<T>.get(key: String, getValue: () -> T? = { null }, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>) -> Unit)? = null) {
    val fetcher = SimpleFetcher(key, getValue)
    get(fetcher, configName, handler)
}

@JvmName("getWithType")
fun <T : Any> Cache<T>.get(key: String, getValue: () -> T? = { null }, configName: String = Config.DEFAULT_NAME, handler: ((Result<Pair<T, Cache.Type>, Exception>) -> Unit)? = null) {
    val fetcher = SimpleFetcher(key, getValue)
    get(fetcher, configName, handler)
}


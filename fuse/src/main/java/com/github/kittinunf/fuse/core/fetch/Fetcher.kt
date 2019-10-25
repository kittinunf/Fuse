package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.result.Result

interface Fetcher<out T : Any> {

    val key: String

    fun fetch(handler: (Result<T, Exception>) -> Unit)

    fun cancel() {
    }
}

class SimpleFetcher<out T : Any>(override val key: String, private val getValue: () -> T?) :
    Fetcher<T> {

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        handler(Result.of(getValue(), { KotlinNullPointerException() }))
    }
}

class NotFoundException(key: String) : RuntimeException("value from $key is not found")

class NoFetcher<out T : Any>(override val key: String) : Fetcher<T> {

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        handler(Result.error(NotFoundException(key)))
    }
}

fun <T : Any> Cache<T>.get(
    key: String,
    getValue: (() -> T?)? = null,
    handler: ((Result<T, Exception>) -> Unit)? = null
) {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    get(fetcher, handler)
}

fun <T : Any> Cache<T>.get(
    key: String,
    getValue: (() -> T?)? = null,
    handler: ((Result<T, Exception>, Cache.Source) -> Unit)? = null
) {
    val fetcher = if (getValue == null) NoFetcher<T>(key) else SimpleFetcher(key, getValue)
    get(fetcher, handler)
}

package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.result.Result

interface Fetcher<out T : Any> {

    val key: String

    fun fetch(): Result<T, Exception>

    fun cancel() {
    }
}

class NotFoundException(key: String) : RuntimeException("value from $key is not found")

internal class SimpleFetcher<out T : Any>(
    override val key: String,
    private val getValue: () -> T?
) : Fetcher<T> {

    override fun fetch(): Result<T, Exception> = Result.of(getValue(), { NotFoundException(key) })
}

internal class NoFetcher<out T : Any>(override val key: String) : Fetcher<T> {

    override fun fetch(): Result<T, Exception> = Result.error(NotFoundException(key))
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

fun <T : Any> Cache<T>.put(
    key: String,
    putValue: T? = null,
    handler: ((Result<T, Exception>) -> Unit)? = null
) {
    val fetcher = if (putValue == null) NoFetcher<T>(key) else SimpleFetcher(key, { putValue })
    put(fetcher, handler)
}

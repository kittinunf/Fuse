package com.github.kittinunf.fuse.core.fetcher

import com.github.kittinunf.result.Result

interface Fetcher<out T : Any> {

    val key: String

    fun fetch(): Result<T, Exception>

    fun cancel() {
    }
}

internal class SimpleFetcher<out T : Any>(override val key: String, private val getValue: () -> T?) : Fetcher<T> {

    override fun fetch(): Result<T, Exception> =
        if (getValue() == null) Result.failure(RuntimeException("Fetch with Key: $key is failure")) else Result.of(getValue)
}

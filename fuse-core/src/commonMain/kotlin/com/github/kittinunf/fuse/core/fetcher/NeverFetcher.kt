package com.github.kittinunf.fuse.core.fetcher

import com.github.kittinunf.result.Result

internal class NotFoundException(key: String) : RuntimeException("Value with key: $key is not found in cache")

internal class NeverFetcher<out T : Any>(override val key: String) : Fetcher<T> {

    override fun fetch(): Result<T, Exception> = Result.failure(NotFoundException(key))
}

package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.result.Result
import java.io.File

class DiskFetcher<T : Any>(
    private val file: File,
    private val convertible: Fuse.DataConvertible<T>
) : Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    override val key: String = file.path

    private var cancelled: Boolean = false

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        if (cancelled) {
            return
        }

        var bytes = ByteArray(0)

        var hasFailed = false

        try {
            bytes = file.readBytes()
        } catch (ex: Exception) {
            hasFailed = true
            handler(Result.error(ex))
        }

        if (cancelled or hasFailed) {
            return
        }

        handler(Result.of { convertFromData(bytes) })
    }

    override fun cancel() {
        cancelled = true
    }
}

fun <T : Any> Cache<T>.get(file: File, handler: ((Result<T, Exception>) -> Unit)? = null) {
    get(DiskFetcher(file, this), handler)
}

fun <T : Any> Cache<T>.get(
    file: File,
    handler: ((Result<T, Exception>, Cache.Source) -> Unit)? = null
) {
    get(DiskFetcher(file, this), handler)
}

fun <T : Any> Cache<T>.put(file: File, handler: ((Result<T, Exception>) -> Unit)? = null) {
    put(DiskFetcher(file, this), handler)
}

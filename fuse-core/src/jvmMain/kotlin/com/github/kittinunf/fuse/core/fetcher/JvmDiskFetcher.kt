package com.github.kittinunf.fuse.core.fetcher

import com.github.kittinunf.fuse.core.formatter.BinarySerializer
import com.github.kittinunf.result.Result
import kotlinx.serialization.KSerializer
import java.io.InputStream

class JvmDiskFetcher<T : Any>(path: String, private val inputStream: InputStream, private val serializer: KSerializer<T>) :
    Fetcher<T> {

    private var cancelled: Boolean = false

    override val key: String = path

    private val binarySerializer = BinarySerializer()

    override fun fetch(): Result<T, Exception> {
        return Result.of {
            val bytes = inputStream.use { it.readBytes() }
            if (cancelled) throw RuntimeException("Fetcher with $key got cancelled")
            binarySerializer.decodeFromByteArray(serializer, bytes)
        }
    }

    override fun cancel() {
        cancelled = true
    }
}

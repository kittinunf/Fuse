package com.github.kittinunf.fuse.core.fetcher

import com.github.kittinunf.fuse.core.formatter.JsonBinaryConverter
import com.github.kittinunf.result.Result
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import java.io.File

class JvmDiskFetcher<T : Any>(
    private val file: File,
    private val serializer: KSerializer<T>,
    private val format: BinaryFormat = JsonBinaryConverter()
) : Fetcher<T>, BinaryFormat by format {

    private var cancelled: Boolean = false

    override val key: String = file.absolutePath

    override fun fetch(): Result<T, Exception> {
        if (!file.isFile) return Result.failure(IllegalStateException("Given $file is not a File URL."))
        if (!file.exists()) return Result.failure(RuntimeException("Given $file is unreachable."))

        return Result.of {
            val bytes = file.inputStream().use { it.readBytes() }
            if (cancelled) throw RuntimeException("Fetcher with $key got cancelled")
            if (bytes.isEmpty()) throw RuntimeException("Cannot read from file")

            decodeFromByteArray(serializer, bytes)
        }
    }

    override fun cancel() {
        cancelled = true
    }
}

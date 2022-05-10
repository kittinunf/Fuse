package com.github.kittinunf.fuse.core.fetcher

import com.github.kittinunf.fuse.core.formatter.JsonBinaryFormatter
import com.github.kittinunf.fuse.core.persistence.toByteArray
import com.github.kittinunf.result.Result
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL

class IosDiskFetcher<T : Any>(
    private val url: NSURL,
    private val binaryFormat: BinaryFormat = JsonBinaryFormatter(),
    private val serializer: KSerializer<T>
) : Fetcher<T>, BinaryFormat by binaryFormat {

    override val key: String = url.absoluteString!!

    override fun fetch(): Result<T, Exception> {
        if (!url.fileURL) return Result.failure(IllegalStateException("Given $url is not a File URL."))

        memScoped {
            val errorPtr = alloc<ObjCObjectVar<NSError?>>()
            if (!url.checkResourceIsReachableAndReturnError(errorPtr.ptr)) return Result.failure(IllegalStateException("Given $url is unreachable. ${errorPtr.value}"))
        }

        return Result.of {
            val data = NSData.dataWithContentsOfURL(url)
            val bytes = data?.toByteArray() ?: throw RuntimeException("Cannot read from file")

            decodeFromByteArray(serializer, bytes)
        }
    }
}

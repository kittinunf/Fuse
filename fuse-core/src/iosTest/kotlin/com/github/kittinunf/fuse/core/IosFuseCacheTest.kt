package com.github.kittinunf.fuse.core

import com.github.kittinunf.result.Result
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class IosFuseCacheTest {

    private val cache: Cache<ByteArray> =
        IosConfig("test-cache", path = null, serializer = ByteArraySerializer(), formatter = object : BinaryFormat {
            override val serializersModule: SerializersModule = EmptySerializersModule

            override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
                return bytes as T
            }

            override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
                return value as ByteArray
            }

        }).build()

    @Test
    fun `should fetch data from file correctly`() {
        val fileUrl = getUrlFromResource("sample_song", "mp3")

        val result = cache.get(fileUrl)
        assertIs<Result.Success<*>>(result)

        val (value, _) = result
        assertNotNull(value)
    }

    @Test
    fun `should put data from file as binary correctly`() {
        val file = getUrlFromResource("sample_song", "mp3")
        val result = cache.put(file)
        assertIs<Result.Success<*>>(result)
    }

    private fun getUrlFromResource(name: String, type: String): NSURL {
        val filePath = NSBundle.mainBundle.pathForResource(name, ofType = type, inDirectory = "resources")
        return NSURL(fileURLWithPath = filePath!!)
    }
}

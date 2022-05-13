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

class IosFuseByteCacheTest {

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
        val filePath = NSBundle.mainBundle.pathForResource("sample_song", ofType = "mp3", inDirectory = "resources")
        val fileUrl = NSURL(fileURLWithPath = filePath!!)

        val result = cache.get(fileUrl)
        assertIs<Result.Success<*>>(result)

        val (value, _) = result
        assertNotNull(value)
    }
}

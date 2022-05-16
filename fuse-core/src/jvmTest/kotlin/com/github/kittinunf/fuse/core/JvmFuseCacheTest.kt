package com.github.kittinunf.fuse.core

import com.github.kittinunf.result.Result
import junit.framework.Assert.assertNotNull
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.junit.Test
import java.io.File
import kotlin.test.assertIs

class JvmFuseCacheTest {

    private val cache: Cache<ByteArray> =
        JvmConfig("test-cache", path = null, serializer = ByteArraySerializer(), formatter = object : BinaryFormat {
            override val serializersModule: SerializersModule = EmptySerializersModule

            override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
                return bytes as T
            }

            override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
                return value as ByteArray
            }

        }).build()

    private val stringCache: Cache<String> =
        JvmConfig("test-string-cache", path = null, serializer = String.serializer(), formatter = object : BinaryFormat {
            override val serializersModule: SerializersModule = EmptySerializersModule

            override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
                return bytes.decodeToString() as T
            }

            override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
                return (value as String).encodeToByteArray()
            }

        }).build()

    @Test
    fun `should fetch data from file as binary correctly`() {
        val file = getFileFromResource("./sample_song.mp3")
        val result = cache.get(file)
        assertIs<Result.Success<*>>(result)

        val (value, _) = result
        assertNotNull(value)
    }

    @Test
    fun `should put data from file as binary correctly`() {
        val file = getFileFromResource("./sample_song.mp3")
        val result = cache.put(file)
        assertIs<Result.Success<*>>(result)
    }

    private fun getFileFromResource(name: String): File {
        return File(ClassLoader.getSystemResource(name).file)
    }
}

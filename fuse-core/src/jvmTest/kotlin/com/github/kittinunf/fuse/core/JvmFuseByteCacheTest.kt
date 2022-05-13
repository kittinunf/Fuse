package com.github.kittinunf.fuse.core

import com.github.kittinunf.result.Result
import junit.framework.Assert.assertNotNull
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.junit.Test
import java.io.File
import kotlin.test.assertIs

class JvmFuseByteCacheTest {

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

    @Test
    fun `should fetch data from file correctly`() {
        val url = ClassLoader.getSystemResource("./sample_song.mp3")
        val file = File(url.file)

        val result = cache.get(file)
        assertIs<Result.Success<*>>(result)

        val (value, _) = result
        assertNotNull(value)
    }
}

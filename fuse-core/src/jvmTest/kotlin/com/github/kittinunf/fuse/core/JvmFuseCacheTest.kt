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
import kotlin.test.assertEquals
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

        val (value, _) = cache.get(file)
        assertNotNull(value)
    }

    @Test
    fun `should fetch data from file as string correctly`() {
        val file = getFileFromResource("lorem_ipsum.txt")
        val result = stringCache.put(file)
        assertIs<Result.Success<*>>(result)

        val (value, _) = stringCache.get(file)
        assertEquals(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce euismod orci at libero sollicitudin, quis mollis nunc rutrum. Proin orci arcu, faucibus non lorem eu, euismod lobortis ligula. Curabitur vehicula lorem nec aliquam mollis. Donec sit amet ligula quis nisi ullamcorper mattis nec eget justo. Sed nec nulla eu nunc hendrerit mattis. Morbi commodo dapibus nibh, eget mattis odio ornare non. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Morbi sed tellus id magna viverra viverra. Ut dignissim quis ligula ac efficitur. Maecenas eleifend faucibus laoreet. Etiam at justo in nulla eleifend cursus. Sed ut molestie ante, nec consectetur turpis. Curabitur imperdiet pharetra mauris at iaculis. Proin a mauris ex. Duis leo nisl, viverra vel mauris eu, convallis ultricies nisl. Nullam vehicula ullamcorper erat vitae dictum.",
            value
        )
    }

    private fun getFileFromResource(name: String): File {
        return File(ClassLoader.getSystemResource(name).file)
    }
}

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

class IosFuseByteCacheTest {

    private val cache: Cache<ByteArray> = IosConfig("test-cache", path = null, serializer = ByteArraySerializer(), formatter = object : BinaryFormat {
        override val serializersModule: SerializersModule = EmptySerializersModule

        override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
            println("decodeFromByteArray $bytes")
            return bytes as T
        }

        override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
            println("encodeToByteArray $value")
            return value as ByteArray
        }

    }).build()

    @Test
    fun `should fetch data from file correctly`() {
        val filePath = NSBundle.mainBundle.pathForResource("sample_song", ofType = "mp3", inDirectory = "resources")
        val fileUrl = NSURL(fileURLWithPath = filePath!!)
        val result = cache.get(fileUrl)
        println(result)
        assertIs<Result.Success<*>>(result)
//        val (value, error) = result
//        assertNotNull(value)
//        assertNull(error)
    }

//    @Test
//    fun fetchFileImageSuccess() {
//        val image = assetDir.resolve("sample.jpg")
//
//        val (value, error) = cache.get(image)
//
//        assertThat(value, notNullValue())
//        assertThat(error, nullValue())
//    }
//
//    @Test
//    fun fetchFileFail() {
//        val song = assetDir.resolve("not_found_song.mp3")
//
//        val (value, error) = cache.get(song)
//
//        assertThat(value, nullValue())
//        assertThat(error, notNullValue())
//    }
//
//    @Test
//    fun checkTimestamp() {
//        cache.get("timestamp", { System.currentTimeMillis().toString().toByteArray() })
//
//        Thread.sleep(2100)
//
//        val timestamp = cache.getTimestamp("timestamp")
//
//        assertThat(timestamp, notNullValue())
//        assertThat(timestamp, not(equalTo(-1L)))
//
//        val timeLimit = 2000L
//        assertThat(
//            System.currentTimeMillis() - timestamp!!,
//            object : BaseMatcher<Long>() {
//                override fun describeTo(description: Description) {
//                    description.appendText("$timestamp is over than $timeLimit")
//                }
//
//                override fun matches(item: Any?): Boolean = (item as Long) > timeLimit
//            }
//        )
//    }
//
//    @Test
//    fun remove() {
//        val (result, source) = cache.getWithSource("YOYO", { "yoyo".toByteArray() })
//        val (value, error) = result
//
//        assertThat(value, notNullValue())
//        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("yoyo"))
//        assertThat(error, nullValue())
//        assertThat(source, equalTo(Source.ORIGIN))
//
//        cache.remove("YOYO", Source.MEM)
//        cache.remove("YOYO", Source.DISK)
//
//        val (anotherValue, anotherError) = cache.get("YOYO")
//
//        assertThat(anotherValue, nullValue())
//        assertThat(anotherError, notNullValue())
//        assertThat(anotherError as NotFoundException, isA(NotFoundException::class.java))
//    }
//
//    @Test
//    fun removeFromMem() {
//        cache.put("remove", "test".toByteArray())
//
//        val result = cache.remove("remove")
//        assertThat(result, equalTo(true))
//
//        val anotherResult = cache.remove("remove")
//        assertThat(anotherResult, equalTo(false))
//    }
//
//    @Test
//    fun removeFromDisk() {
//        cache.put("remove", "test".toByteArray())
//
//        val result = cache.remove("remove", Source.DISK)
//        assertThat(result, equalTo(true))
//
//        val anotherResult = cache.remove("remove", Source.MEM)
//        assertThat(anotherResult, equalTo(true))
//
//        val hasKey = cache.hasKey("remove")
//        assertThat(hasKey, equalTo(false))
//    }
//
//    @Test
//    fun removeThemAll() {
//        val count = 10
//        val lock = CountDownLatch(count)
//
//        (1..count).forEach {
//            cache.put("remove $it", "yoyo".toByteArray())
//        }
//        lock.wait()
//
//        assertThat(cache.allKeys(), not(matchesEmpty(cache.allKeys())) as Matcher<in Set<String>>)
//        (1..count).forEach {
//            assertThat(
//                cache.allKeys(),
//                hasItems("remove $it")
//            )
//        }
//        cache.removeAll()
//        assertThat(cache.allKeys(), matchesEmpty(cache.allKeys()) as Matcher<in Set<String>>)
//    }
//
//    private inline fun <reified T> matchesEmpty(collection: Collection<T>) = object : BaseMatcher<T>() {
//        override fun describeTo(description: Description) {
//            description.appendText("$collection is not empty")
//        }
//
//        override fun matches(item: Any?): Boolean = (item as Collection<T>).isEmpty()
//    }
}

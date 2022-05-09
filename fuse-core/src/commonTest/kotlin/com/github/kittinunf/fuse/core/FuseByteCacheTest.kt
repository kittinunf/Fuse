package com.github.kittinunf.fuse.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal expect fun createByteTestCache(name: String, context: Any): Cache<ByteArray>

class FuseByteCacheTest : BaseTest() {

    lateinit var cache: Cache<ByteArray>

    override fun setUp(any: Any) {
        cache = createByteTestCache("test", any)
        cache.removeAll()
    }

    @Test
    fun `should fetch data correctly with defaultValue`() {
        val (result, source) = cache.getWithSource("hello", defaultValue = { "world".encodeToByteArray() })
        val (value, error) = result

        assertNotNull(value)
        assertEquals("world", value.decodeToString())
        assertNull(error)
        assertEquals(Source.ORIGIN, source)
    }

    @Test
    fun `should return hasKey as true when there is data with key in the cache`() {
        val (value, error) = cache.get("hello", defaultValue = { "world".encodeToByteArray() })
        val hasKey = cache.hasKey("hello")

        assertNotNull(value)
        assertEquals(value.decodeToString(), "world")
        assertNull(error)
        assertTrue(hasKey)

        val notFound = cache.hasKey("xxxx")
        assertFalse(notFound)
    }

    @Test
    fun `should return failure when fetch function is null out`() {
        fun fetchFail(): ByteArray? = null

        val (result, source) = cache.getWithSource("fail", ::fetchFail)
        val (value, error) = result

        assertNull(value)
        assertNotNull(error)
        assertEquals(Source.ORIGIN, source)
    }

    @Test
    fun `should return source as memory after fetching the second time`() {
        // get this once, to trigger save in memory
        cache.getWithSource("hello", defaultValue = { "world".encodeToByteArray() })

        val (result, source) = cache.getWithSource("hello", defaultValue = { "world".encodeToByteArray() })
        val (value, error) = result

        assertNotNull(value)
        assertEquals("world", value.decodeToString())
        assertNull(error)
        assertEquals(Source.MEM, source)
    }

    @Test
    fun `should fallback to use disk value when we remove it from memory`() {
        cache.getWithSource("hello", defaultValue = { "world".encodeToByteArray() })
        // remove from memory cache
        cache.remove("hello", Source.MEM)

        val (result, source) = cache.getWithSource("hello")
        val (value, error) = result

        assertNotNull(value)
        assertEquals("world", value.decodeToString())
        assertNull(error)
        assertEquals(Source.DISK, source)
    }

//    @Test
//    fun putStringSuccess1() {
//        val (value, error) = cache.put("Test Put", "Hello world".toByteArray())
//
//        assertThat(value, notNullValue())
//        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("Hello world"))
//        assertThat(error, nullValue())
//    }
//
//    @Test
//    fun putStringSuccess2() {
//        // this needs to be run sequentially after running the putStringSuccess1
//        val (value, error) = cache.get("Test Put")
//
//        assertThat(value, notNullValue())
//        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("Hello world"))
//        assertThat(error, nullValue())
//    }
//
//    @Test
//    fun fetchFileSuccess() {
//        val song = assetDir.resolve("sample_song.mp3")
//
//        val (value, error) = cache.get(song)
//
//        assertThat(value, notNullValue())
//        assertThat(error, nullValue())
//    }
//
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

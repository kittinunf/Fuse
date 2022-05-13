package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetcher.NotFoundException
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
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

    @Test
    fun `should put value into the cache successfully`() {
        val (value, error) = cache.put("put", "Hello world".encodeToByteArray())

        assertNotNull(value)
        assertEquals("Hello world", value.decodeToString())
        assertNull(error)

        val (value2, error2) = cache.get("put")

        assertNotNull(value2)
        assertNull(error2)
        assertEquals("Hello world", value2.decodeToString())
    }

//    @Test
//    fun test() {
//        val song = readResource("./sample_song.mp3")
//        println(song)
//
//        val (value, error) = cache.get(song)
//
//        assertThat(value, notNullValue())
//        assertThat(error, nullValue())
//    }

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
    @Test
    fun `should get correct timestamp`() {
        val timeStamp = Clock.System.now().toEpochMilliseconds()
        cache.get("timestamp", { timeStamp.toString().encodeToByteArray() })

        val retrieved = cache.getTimestamp("timestamp")

        assertNotNull(retrieved)
        assertNotEquals(-1, retrieved)
        assertTrue { (timeStamp - retrieved) < 1_000 }
    }

    @Test
    fun `should able to remove item correctly`() {
        val (result, source) = cache.getWithSource("YOYO", { "yoyo".encodeToByteArray() })
        val (value, error) = result

        assertNotNull(value)
        assertEquals("yoyo", value.decodeToString())
        assertNull(error)
        assertEquals(Source.ORIGIN, source)

        cache.remove("YOYO", Source.MEM)
        cache.remove("YOYO", Source.DISK)

        val (anotherValue, anotherError) = cache.get("YOYO")

        assertNull(anotherValue)
        assertNotNull(anotherError)
        assertIs<NotFoundException>(anotherError)
    }

    @Test
    fun `should be able to remove from mem correctly`() {
        cache.put("remove", "test".encodeToByteArray())

        val result = cache.remove("remove")
        assertTrue(result)

        val anotherResult = cache.remove("remove")
        assertFalse(anotherResult)
    }

    @Test
    fun `should be able to remove from disk correctly`() {
        cache.put("remove", "test".encodeToByteArray())

        val result = cache.remove("remove", Source.DISK)
        assertTrue(result)

        val anotherResult = cache.remove("remove", Source.MEM)
        assertTrue(anotherResult)

        val hasKey = cache.hasKey("remove")
        assertFalse(hasKey)
    }

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

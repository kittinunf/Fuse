package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.ByteArrayDataConvertible
import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.NotFoundException
import com.github.kittinunf.fuse.core.get
import com.github.kittinunf.fuse.core.getWithSource
import com.github.kittinunf.fuse.core.put
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.greaterThan
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FuseByteCacheTest : BaseTestCase() {

    companion object {
        private val tempDir = createTempDir().absolutePath
        val cache =
            CacheBuilder.config(tempDir, "Byte", ByteArrayDataConvertible()) {
                // do more configuration here
            }.build()
    }

    private var hasSetUp = false

    @Before
    fun initialize() {
        if (!hasSetUp) {
            hasSetUp = true
        }
    }

    @Test
    fun fetch() {
        val (result, source) = cache.getWithSource("hello", { "world".toByteArray() })
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("world"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))
    }

    @Test
    fun hasKey() {
        val (value, error) = cache.get("hello")

        val hasKey = cache.hasKey("hello")

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("world"))
        assertThat(error, nullValue())
        assertThat(hasKey, equalTo(true))
    }

    @Test
    fun fetchSecondFail() {
        fun fetchFail(): ByteArray? = null

        val (result, source) = cache.getWithSource("fail", ::fetchFail)
        val (value, error) = result

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))
    }

    @Test
    fun fetchFromMemory() {
        val (result, source) = cache.getWithSource("hello", { "world".toByteArray() })
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("world"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.MEM))
    }

    @Test
    fun fetchFromDisk() {
        val (result, source) = cache.getWithSource("hello", { "world".toByteArray() })
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("world"))
        assertThat(error, nullValue())

        // remove from memory cache
        cache.remove("hello", Cache.Source.MEM)

        val (result2, source2) = cache.getWithSource("hello", { "world".toByteArray() })
        val (value2, error2) = result2

        assertThat(value2, notNullValue())
        assertThat(value2!!.toString(Charset.defaultCharset()), equalTo("world"))
        assertThat(error2, nullValue())
        assertThat(source2, equalTo(Cache.Source.DISK))
    }

    @Test
    fun putStringSuccess1() {
        val (value, error) = cache.put("Test Put", "Hello world".toByteArray())

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("Hello world"))
        assertThat(error, nullValue())
    }

    @Test
    fun putStringSuccess2() {
        // this needs to be run sequentially after running the putStringSuccess1
        val (value, error) = cache.get("Test Put")

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("Hello world"))
        assertThat(error, nullValue())
    }

    @Test
    fun fetchFileSuccess() {
        val song = assetDir.resolve("sample_song.mp3")

        val (value, error) = cache.get(song)

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun fetchFileImageSuccess() {
        val image = assetDir.resolve("sample.jpg")

        val (value, error) = cache.get(image)

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun fetchFileFail() {
        val song = assetDir.resolve("not_found_song.mp3")

        val (value, error) = cache.get(song)

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
    }

    @Test
    fun checkTimestamp() {
        cache.get("timestamp", { System.currentTimeMillis().toString().toByteArray() })

        Thread.sleep(2100)

        val timestamp = cache.getTimestamp("timestamp")

        assertThat(timestamp, not(equalTo(-1L)))
        assertThat(System.currentTimeMillis() - timestamp, greaterThan(2000L))
    }

    @Test
    fun remove() {
        val (result, source) = cache.getWithSource("YOYO", { "yoyo".toByteArray() })
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("yoyo"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))

        cache.remove("YOYO", Cache.Source.MEM)
        cache.remove("YOYO", Cache.Source.DISK)

        val (anotherValue, anotherError) = cache.get("YOYO")

        assertThat(anotherValue, nullValue())
        assertThat(anotherError, notNullValue())
        assertThat(anotherError as NotFoundException, isA(NotFoundException::class.java))
    }

    @Test
    fun removeFromMem() {
        cache.put("remove", "test".toByteArray())

        val result = cache.remove("remove")
        assertThat(result, equalTo(true))

        val anotherResult = cache.remove("remove")
        assertThat(anotherResult, equalTo(false))
    }

    @Test
    fun removeFromDisk() {
        cache.put("remove", "test".toByteArray())

        val result = cache.remove("remove", Cache.Source.DISK)
        assertThat(result, equalTo(true))

        val anotherResult = cache.remove("remove", Cache.Source.MEM)
        assertThat(anotherResult, equalTo(true))

        val hasKey = cache.hasKey("remove")
        assertThat(hasKey, equalTo(false))
    }

    @Test
    fun removeThemAll() {
        val count = 10
        val lock = CountDownLatch(count)

        (1..count).forEach {
            cache.put("remove $it", "yoyo".toByteArray())
        }
        lock.wait()

        assertThat(cache.allKeys(), not(empty()))
        (1..count).forEach {
            assertThat(
                cache.allKeys(),
                hasItems("remove $it")
            )
        }
        cache.removeAll()
        assertThat(cache.allKeys(), empty())
    }
}

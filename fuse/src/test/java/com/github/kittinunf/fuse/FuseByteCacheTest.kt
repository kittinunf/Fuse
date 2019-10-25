package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.ByteArrayDataConvertible
import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.get
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import org.hamcrest.CoreMatchers.`is` as isEqualTo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FuseByteCacheTest : BaseTestCase() {

    companion object {
        private val tempDir = createTempDir().absolutePath
        val cache =
            CacheBuilder.config<ByteArray>(tempDir) { callbackExecutor = Executor { it.run() } }
                .build(ByteArrayDataConvertible())
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
        val lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.NOT_FOUND))
    }

    @Test
    fun secondFetchFail() {
        val lock = CountDownLatch(1)

        fun fetchFail(): ByteArray? = null

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get("fail") { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.NOT_FOUND))
    }

    @Test
    fun fetchFromMemory() {
        val lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.MEM))
    }

    @Test
    fun fetchFromDisk() {
        var lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())

        // remove from memory cache
        cache.remove("hello", true)

        lock = CountDownLatch(1)
        cache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.DISK))
    }

    @Test
    fun putStringSuccess1() {
        val lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null

        cache.put("Test Put", "Hello world".toByteArray()) {
            value = it.component1()
            error = it.component2()
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), equalTo("Hello world"))
        assertThat(error, nullValue())
    }

    @Test
    fun putStringSuccess2() {
        // this needs to be run sequentially after running the putStringSuccess1
        val anotherLock = CountDownLatch(1)
        var anotherValue: ByteArray? = null
        var anotherError: Exception? = null
        cache.get("Test Put") { result ->
            anotherValue = result.component1()
            anotherError = result.component2()
            anotherLock.countDown()
        }
        anotherLock.wait()

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue!!.toString(Charset.defaultCharset()), equalTo("Hello world"))
        assertThat(anotherError, nullValue())
    }

    @Test
    fun fetchFileSuccess() {
        val lock = CountDownLatch(1)
        val song = assetDir.resolve("sample_song.mp3")

        var value: ByteArray? = null
        var error: Exception? = null

        cache.get(song) { result ->
            val (v, e) = result
            value = v
            error = e
            lock.countDown()
        }

        lock.wait()

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
    }

    @Test
    fun fetchFileFail() {
        val lock = CountDownLatch(1)
        val song = assetDir.resolve("not_found_song.mp3")

        var value: ByteArray? = null
        var error: Exception? = null

        cache.get(song) { result ->
            val (v, e) = result
            value = v
            error = e
            lock.countDown()
        }

        lock.wait()

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
    }
}

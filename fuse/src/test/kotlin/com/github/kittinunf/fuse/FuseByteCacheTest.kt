package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class FuseByteCacheTest : BaseTestCase() {

    var hasSetUp = false

    @Before
    fun initialize() {
        if (!hasSetUp) {
            hasSetUp = true

            Fuse.init(tempDirString)
            Fuse.callbackExecutor = Executor { it.run() }
        }
    }

    @Test
    fun firstFetch() {
        val lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.bytesCache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.NOT_FOUND))
    }

    @Test
    fun SecondFetchFail() {
        val lock = CountDownLatch(1)

        fun fetchFail(): ByteArray? = null

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.bytesCache.get("fail", ::fetchFail) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.NOT_FOUND))
    }

    @Test
    fun fetchFromMemory() {
        val lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.bytesCache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.MEM))
    }

    @Test
    fun fetchFromDisk() {
        var lock = CountDownLatch(1)

        var value: ByteArray? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.bytesCache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())

        //remove from memory cache
        Fuse.bytesCache.remove("hello", true)

        lock = CountDownLatch(1)
        Fuse.bytesCache.get("hello", { "world".toByteArray() }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value!!.toString(Charset.defaultCharset()), isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.DISK))
    }

    @Test
    fun fetchFileSuccess() {
        val lock = CountDownLatch(1)
        val song = assetDir.resolve("sample_song.mp3")

        var value: ByteArray? = null
        var error: Exception? = null

        Fuse.bytesCache.get(song) { result ->
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

        Fuse.bytesCache.get(song) { result ->
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
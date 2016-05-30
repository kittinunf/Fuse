package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class FuseStringCacheTest : BaseTestCase() {

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

        var value: String? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.stringCache.get("hello", { "world" }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.NOT_FOUND))
    }

    @Test
    fun fetchFromMemory() {
        var lock = CountDownLatch(1)

        val loremFile = assetDir.resolve("lorem_ipsum.txt")

        var value: String? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.stringCache.get(loremFile) { result ->
            val (v, e) = result
            value = v
            error = e
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(error, nullValue())

        lock = CountDownLatch(1)
        Fuse.stringCache.get(loremFile) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, startsWith("Lorem ipsum dolor sit amet,"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.MEM))
    }

    @Test
    fun fetchFromNetwork() {
        var lock = CountDownLatch(1)
        val url = URL("http://www.google.com")

        var value: String? = null
        var error: Exception? = null
        var cacheType: Cache.Type? = null

        Fuse.stringCache.get(url) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, containsString("<title>Google</title>"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.NOT_FOUND))

        // fetch again
        lock = CountDownLatch(1)
        Fuse.stringCache.get(url) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheType = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, containsString("<title>Google</title>"))
        assertThat(error, nullValue())
        assertThat(cacheType, isEqualTo(Cache.Type.MEM))
    }

}
package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.get
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import org.hamcrest.CoreMatchers.`is` as isEqualTo
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.startsWith
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class FuseStringCacheTest : BaseTestCase() {

    companion object {
        private val tempDir = createTempDir().absolutePath
        val cache = CacheBuilder.config(tempDir, StringDataConvertible()) {
            callbackExecutor = Executor { it.run() }
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
    fun firstFetch() {
        val lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get("hello", { "world" }) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, isEqualTo("world"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.ORIGIN))
    }

    @Test
    fun fetchFromMemory() {
        var lock = CountDownLatch(1)

        val loremFile = assetDir.resolve("lorem_ipsum.txt")

        var value: String? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get(loremFile) { result ->
            val (v, e) = result
            value = v
            error = e
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(error, nullValue())

        lock = CountDownLatch(1)
        cache.get(loremFile) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, startsWith("Lorem ipsum dolor sit amet,"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.MEM))
    }

    @Test
    fun fetchFromNetwork() {
        var lock = CountDownLatch(1)
        val url = URL("http://www.google.com")

        var value: String? = null
        var error: Exception? = null
        var cacheSource: Cache.Source? = null

        cache.get(url) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, containsString("<title>Google</title>"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.ORIGIN))

        // fetch again
        lock = CountDownLatch(1)
        cache.get(url) { result, type ->
            val (v, e) = result
            value = v
            error = e
            cacheSource = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, containsString("<title>Google</title>"))
        assertThat(error, nullValue())
        assertThat(cacheSource, isEqualTo(Cache.Source.MEM))
    }

    @Test
    fun applyValue() {
        val lock = CountDownLatch(1)

        val cache = CacheBuilder.config(tempDir, "Custom", StringDataConvertible()) {
            callbackExecutor = Executor { it.run() }
            transformer = { _, value -> value.toUpperCase() + "1" }
        }.build()

        var value: String? = null
        var error: Exception? = null

        cache.get("hello", { "world" }) { result ->
            val (v, e) = result
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, isEqualTo("WORLD1"))
        assertThat(error, nullValue())
    }

    @Test
    fun applyValueForSomeKey() {
        var lock = CountDownLatch(1)

        val cache = CacheBuilder.config(tempDir, "Another Custom", StringDataConvertible()) {
            callbackExecutor = Executor { it.run() }
            transformer = { key, value ->
                if (key == "custom") value.toUpperCase()
                else value
            }
        }.build()

        var value: String? = null
        var error: Exception? = null

        cache.get("hello", { "world" }) { result ->
            val (v, e) = result
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, isEqualTo("world"))
        assertThat(error, nullValue())

        lock = CountDownLatch(1)
        cache.get("custom", { "world" }) { result ->
            val (v, e) = result
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, isEqualTo("WORLD"))
        assertThat(error, nullValue())
    }
}

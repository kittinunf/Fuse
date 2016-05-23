package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
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
}
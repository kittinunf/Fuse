package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.get
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class FuseTest : BaseTestCase() {

    init {
        lock = CountDownLatch(1)
        Fuse.init(File.createTempFile("tmp", null).parent)
        Fuse.backgroundExecutor = Executors.newSingleThreadExecutor { r ->
            r.run()
            Thread.currentThread()
        }
    }

    @Test
    fun simpleFetchTestBytes() {
        Fuse.bytesCache.get("hello", { "world".toByteArray() }) { result ->
            val (v, e) = result
            assertThat(v, notNullValue())
            assertThat(v!!.toString(Charset.defaultCharset()), isEqualTo("worl"))
            assertThat(e, nullValue())
        }
    }

}
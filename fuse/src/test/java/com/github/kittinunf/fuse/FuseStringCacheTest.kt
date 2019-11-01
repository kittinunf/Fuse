package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.get
import com.github.kittinunf.fuse.core.getWithSource
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class FuseStringCacheTest : BaseTestCase() {

    companion object {
        private val tempDir = createTempDir().absolutePath
        val cache = CacheBuilder.config(tempDir, StringDataConvertible()).build()
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
        val (result, source) = cache.getWithSource("hello", { "world" })
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))
    }

    @Test
    fun applyValue() {
        val cache = CacheBuilder.config(tempDir, "Custom", StringDataConvertible()) {
            transformer = { _, value -> value.toUpperCase() + "1" }
        }.build()

        val (value, error) = cache.get("hello", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("WORLD1"))
        assertThat(error, nullValue())
    }

    @Test
    fun applyValueForSomeKey() {
        val cache = CacheBuilder.config(tempDir, "Another Custom", StringDataConvertible()) {
            transformer = { key, value ->
                if (key == "custom") value.toUpperCase()
                else value
            }
        }.build()

        val (value, error) = cache.get("hello", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        val (anotherResult, anotherSource) = cache.getWithSource("custom", { "world" })
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("WORLD"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, equalTo(Cache.Source.ORIGIN))
    }
}

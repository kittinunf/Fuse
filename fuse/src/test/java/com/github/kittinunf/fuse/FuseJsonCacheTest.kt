package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.DiskFetcher
import com.github.kittinunf.fuse.core.get
import com.github.kittinunf.fuse.core.put
import com.github.kittinunf.fuse.model.HttpbinGet
import com.github.kittinunf.fuse.model.SampleProduct
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileNotFoundException
import java.net.URL
import java.nio.charset.Charset

class FuseJsonCacheTest : BaseTestCase() {

    companion object {
        private val tempDir = createTempDir().absolutePath
    }

    private var hasSetUp = false

    @Before
    fun initialize() {
        if (!hasSetUp) {
            hasSetUp = true
        }
    }

    class ProductDataConvertible(private val charset: Charset = Charset.defaultCharset()) :
        Fuse.DataConvertible<SampleProduct> {
        override fun convertFromData(bytes: ByteArray): SampleProduct =
            Json.decodeFromString(bytes.toString(charset))

        override fun convertToData(value: SampleProduct): ByteArray = Json.encodeToString(value).toByteArray(charset)
    }

    class HttpbinGetDataConvertible(private val charset: Charset = Charset.defaultCharset()) :
        Fuse.DataConvertible<HttpbinGet> {
        override fun convertFromData(bytes: ByteArray): HttpbinGet =
            Json.decodeFromString(bytes.toString(charset))

        override fun convertToData(value: HttpbinGet): ByteArray = Json.encodeToString(value).toByteArray(charset)
    }

    @Test
    fun firstFetch() {
        val json = assetDir.resolve("sample.json")

        val cache = CacheBuilder.config(tempDir, ProductDataConvertible()).build()
        val (value, error) = cache.get(json)

        assertThat(value, notNullValue())
        assertThat(value!!.name, equalTo("Product"))
        assertThat(error, nullValue())
    }

    @Test
    fun fetchFromNetworkSuccess() {
        val httpBin = URL("https://www.httpbin.org/get")


        val cache = CacheBuilder.config(tempDir, HttpbinGetDataConvertible()).build()

        val (result, source) = cache.getWithSource(httpBin)
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value!!.url, equalTo("https://www.httpbin.org/get"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Source.ORIGIN))

        val (anotherResult, anotherSource) = cache.getWithSource(httpBin)
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue!!.url, equalTo("https://www.httpbin.org/get"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, equalTo(Source.MEM))
    }

    @Test
    fun fetchFromNetworkFail() {
        val httpBin = URL("http://www.httpbin.org/fail")

        val cache = CacheBuilder.config(tempDir, HttpbinGetDataConvertible()).build()

        val (value, error) = cache.get(httpBin)

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(error as? FileNotFoundException, isA(FileNotFoundException::class.java))
    }

    @Test
    fun putWithValueJsonCompatible() {
        val temp = assetDir.resolve("sample.json").copyTo(assetDir.resolve("temp.json"), true)

        val cache = CacheBuilder.config(tempDir, ProductDataConvertible()).build()

        val newText = temp.readText().replace("Product", "New Product")
        temp.writeText(newText)

        val (value, error) = cache.put(temp)

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
        assertThat(value!!.name, equalTo("New Product"))
    }

    @Test
    fun putWithValueJsonNotCompatible() {
        val json = assetDir.resolve("broken_sample.json")

        val cache = CacheBuilder.config(tempDir, ProductDataConvertible()).build()

        val (value, error) = cache.put(DiskFetcher(json, cache))

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
    }
}

package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.DiskFetcher
import com.github.kittinunf.fuse.core.get
import com.github.kittinunf.fuse.core.getWithSource
import com.github.kittinunf.fuse.core.put
import java.io.FileNotFoundException
import java.net.URL
import java.nio.charset.Charset
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class FuseJsonCacheTest : BaseTestCase() {

    class JsonDataConvertible(private val charset: Charset = Charset.defaultCharset()) :
        Fuse.DataConvertible<JSONObject> {
        override fun convertFromData(bytes: ByteArray): JSONObject =
            JSONObject(bytes.toString(charset))

        override fun convertToData(value: JSONObject): ByteArray =
            value.toString().toByteArray(charset)
    }

    companion object {
        private val tempDir = createTempDir().absolutePath
        val cache = CacheBuilder.config(tempDir, JsonDataConvertible()).build()
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
        val json = assetDir.resolve("sample_json.json")

        val (value, error) = cache.get(json)

        assertThat(value, notNullValue())
        assertThat(value!!.getString("name"), equalTo("Product"))
        assertThat(error, nullValue())
    }

    @Test
    fun fetchFromNetworkSuccess() {
        val httpBin = URL("https://www.httpbin.org/get")
        val (result, source) = cache.getWithSource(httpBin)
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(value!!.getString("url"), equalTo("https://www.httpbin.org/get"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Source.ORIGIN))

        val (anotherResult, anotherSource) = cache.getWithSource(httpBin)
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue!!.getString("url"), equalTo("https://www.httpbin.org/get"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, equalTo(Source.MEM))
    }

    @Test
    fun fetchFromNetworkFail() {
        val httpBin = URL("http://www.httpbin.org/fail")
        val (value, error) = cache.get(httpBin)

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(error as? FileNotFoundException, isA(FileNotFoundException::class.java))
    }

    @Test
    fun fetchWithValueJsonNotCompatible() {
        val json = assetDir.resolve("broken_json.json")

        val (result, source) = cache.getWithSource(json)
        val (value, error) = result

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(source, equalTo(Source.ORIGIN))
        assertThat(error as? JSONException, isA(JSONException::class.java))
    }

    @Test
    fun putWithValueJsonCompatible() {
        val temp = assetDir.resolve("sample_json.json").copyTo(assetDir.resolve("temp.json"), true)

        val newText = temp.readText().replace("Product", "New Product")
        temp.writeText(newText)

        val (value, error) = cache.put(temp)

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
        assertThat(value!!.getString("name"), equalTo("New Product"))
    }

    @Test
    fun putWithValueJsonNotCompatible() {
        val json = assetDir.resolve("broken_json.json")

        val (value, error) = cache.put(DiskFetcher(json, cache))

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(error as? JSONException, isA(JSONException::class.java))
    }
}

package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.model.Id
import com.github.kittinunf.fuse.core.model.Price
import com.github.kittinunf.fuse.core.model.Product
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal expect fun createStringTestCache(name: String, context: Any): Cache<String>

class FuseJsonCacheTest : BaseTest() {

    lateinit var cache: Cache<String>

    override fun setUp(any: Any) {
        cache = createStringTestCache("json-test", any)
        cache.removeAll()
    }

    @Serializable
    private data class Name(val name: String, val surname: String)

    @Test
    fun `should fetch from json string correctly`() {
        val json = """
            { "name" : "hello", "surname" : "world" }
        """.trimIndent()

        val (value, error) = cache.get("json", defaultValue = { json })
        assertNotNull(value)
        assertNull(error)

        val (v, e) = cache.get("json")
        assertNotNull(v)
        assertNull(e)
        val serialized = Json.decodeFromString<Name>(v)
        assertEquals("hello", serialized.name)
        assertEquals("world", serialized.surname)
    }

    @Test
    fun `should read data as json string correctly`() {
        val data = readResource("./sample.json")

        val (value, error) = cache.get("json", defaultValue = { data.decodeToString() })
        assertNotNull(value)
        assertNull(error)

        val (result, source) = cache.getWithSource("json")
        assertEquals(Source.MEM, source)
        val (v, e) = result
        assertNotNull(v)
        assertNull(e)
        val serialized = Json.decodeFromString<Product>(v)
        assertEquals("Product", serialized.name)
        assertEquals(Id("number", description = "Product Identifier", required = true), serialized.properties.id)
        assertEquals(Price("number", minimum = 10, required = true), serialized.properties.price)
    }

//    @Test
//    fun fetchFromNetworkSuccess() {
//        val httpBin = URL("https://www.httpbin.org/get")
//
//
//        val cache = CacheBuilder.config(tempDir, HttpbinGetDataConvertible()).build()
//
//        val (result, source) = cache.getWithSource(httpBin)
//        val (value, error) = result
//
//        assertThat(value, notNullValue())
//        assertThat(value!!.url, equalTo("https://www.httpbin.org/get"))
//        assertThat(error, nullValue())
//        assertThat(source, equalTo(Source.ORIGIN))
//
//        val (anotherResult, anotherSource) = cache.getWithSource(httpBin)
//        val (anotherValue, anotherError) = anotherResult
//
//        assertThat(anotherValue, notNullValue())
//        assertThat(anotherValue!!.url, equalTo("https://www.httpbin.org/get"))
//        assertThat(anotherError, nullValue())
//        assertThat(anotherSource, equalTo(Source.MEM))
//    }
//
//    @Test
//    fun fetchFromNetworkFail() {
//        val httpBin = URL("http://www.httpbin.org/fail")
//
//        val cache = CacheBuilder.config(tempDir, HttpbinGetDataConvertible()).build()
//
//        val (value, error) = cache.get(httpBin)
//
//        assertThat(value, nullValue())
//        assertThat(error, notNullValue())
//        assertThat(error as? FileNotFoundException, isA(FileNotFoundException::class.java))
//    }
//
//    @Test
//    fun putWithValueJsonCompatible() {
//        val temp = assetDir.resolve("sample.json").copyTo(assetDir.resolve("temp.json"), true)
//
//        val cache = CacheBuilder.config(tempDir, ProductDataConvertible()).build()
//
//        val newText = temp.readText().replace("Product", "New Product")
//        temp.writeText(newText)
//
//        val (value, error) = cache.put(temp)
//
//        assertThat(value, notNullValue())
//        assertThat(error, nullValue())
//        assertThat(value!!.name, equalTo("New Product"))
//    }
//
//    @Test
//    fun putWithValueJsonNotCompatible() {
//        val json = assetDir.resolve("broken_sample.json")
//
//        val cache = CacheBuilder.config(tempDir, ProductDataConvertible()).build()
//
//        val (value, error) = cache.put(DiskFetcher(json, cache))
//
//        assertThat(value, nullValue())
//        assertThat(error, notNullValue())
//    }
}

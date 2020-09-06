package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.Source
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.scenario.ExpirableCache
import com.github.kittinunf.fuse.core.scenario.get
import com.github.kittinunf.fuse.core.scenario.getWithSource
import com.github.kittinunf.fuse.core.scenario.put
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds
import kotlin.time.seconds

class FuseScenarioTest : BaseTestCase() {

    @ExperimentalTime
    companion object {
        private val tempDir = createTempDir().absolutePath
        private val cache = CacheBuilder.config(tempDir, StringDataConvertible()).build()

        private val expirableCache = ExpirableCache(cache)
    }

    private var hasSetUp = false

    @Before
    fun initialize() {
        if (!hasSetUp) {
            hasSetUp = true
        }
    }

    @ExperimentalTime
    @Test
    fun fetchWhenNoData() {
        // remove first if it exists
        expirableCache.remove("hello", Source.MEM)
        expirableCache.remove("hello", Source.DISK)

        val (result, source) = expirableCache.getWithSource("hello", { "world" })
        val (value, error) = result

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
        assertThat(source, equalTo(Source.ORIGIN))

        val timestamp = expirableCache.getTimestamp("hello")
        assertThat(timestamp, not(equalTo(-1L)))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitExpired() {
        val (value, error) = expirableCache.get("hello", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(600)

        val (anotherValue, anotherError) = expirableCache.get(
            "hello",
            { "new world" },
            timeLimit = 500.milliseconds
        )

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("new world"))
        assertThat(anotherError, nullValue())
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitExpiredWithSource() {
        val (value, error) = expirableCache.get("hello-source", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(600)

        val (anotherResult, anotherSource) = expirableCache.getWithSource(
            "hello-source",
            { "new world" },
            timeLimit = 500.milliseconds
        )
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("new world"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, equalTo(Source.ORIGIN))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitExpiredButStillForceToUse() {
        val (value, error) = expirableCache.get("expired", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(600)

        val (anotherResult, anotherSource) = expirableCache.getWithSource(
            "expired",
            { "new world" },
            timeLimit = 500.milliseconds,
            useEntryEvenIfExpired = true
        )
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("world"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, equalTo(Source.MEM))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitNotExpired() {
        val (value, error) = expirableCache.get("not expired", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(1000)

        val (anotherResult, anotherSource) = expirableCache.getWithSource("not expired", { "new world" }, 5.seconds)
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("world"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, not(equalTo(Source.ORIGIN)))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitNotExpiredButNotInMemory() {
        val (value, error) = expirableCache.get("not expired", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(1000)
        expirableCache.remove("not expired", Source.MEM)

        val (anotherResult, anotherSource) = expirableCache.getWithSource("not expired", { "new world" }, 5.seconds)
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("world"))
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, equalTo(Source.DISK))
    }

    @ExperimentalTime
    @Test
    fun fetchWithFetcherThatWillSuccess() {
        val goodFetcher = object : Fetcher<String> {
            override val key: String = "foofoo"

            override fun fetch(): Result<String, Exception> = Result.success("foofoo2")
        }

        val (value, error) = expirableCache.get(goodFetcher)
        assertThat(value, notNullValue())
        assertThat(value, equalTo("foofoo2"))
        assertThat(error, nullValue())
    }

    @ExperimentalTime
    @Test
    fun fetchWithFetcherThatWillFail() {
        val (value, error) = expirableCache.get("can_fail", { "world" })

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        val failFetcher = object : Fetcher<String> {
            override val key: String = "can_fail"
            override fun fetch(): Result<String, Exception> = Result.error(Exception("fail catcher"))
        }

        // this will always force to be expired
        val (anotherResult, anotherSource) = expirableCache.getWithSource(failFetcher, Duration.ZERO)
        val (anotherValue, anotherError) = anotherResult

        assertThat(anotherValue, notNullValue())
        assertThat(anotherError, nullValue())
        assertThat(anotherSource, not(equalTo(Source.ORIGIN)))
    }

    @ExperimentalTime
    @Test
    fun putWillRetrieveDataFromTheFetcher() {
        val (value, error) = expirableCache.put("foofoo", "foofoo2")

        assertThat(value, notNullValue())
        assertThat(value, equalTo("foofoo2"))
        assertThat(error, nullValue())

        expirableCache.remove("foofoo", Source.MEM)
        expirableCache.remove("foofoo", Source.DISK)

        val goodFetcher = object : Fetcher<String> {
            override val key: String = "foofoo"

            override fun fetch(): Result<String, Exception> = Result.success("foofoo2")
        }

        val (anotherValue, anotherError) = expirableCache.put(goodFetcher)
        assertThat(anotherValue, notNullValue())
        assertThat(anotherValue, equalTo("foofoo2"))
        assertThat(anotherError, nullValue())
    }

    @ExperimentalTime
    @Test
    fun fetchWithFailureFetcherAndAlwaysExpiringEntry() {
        val failFetcher = object : Fetcher<String> {
            override val key: String = "will_fail"

            override fun fetch(): Result<String, Exception> = Result.error(Exception("fail catcher"))
        }

        val (result, source) = expirableCache.getWithSource(failFetcher, Duration.ZERO)
        val (value, error) = result

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(source, equalTo(Source.ORIGIN))
    }

    @ExperimentalTime
    @Test
    fun cleanEmptyCacheWillNotCrash() {
        val (value, error) = expirableCache.put("foofoo", "foofoo2")

        assertThat(value, notNullValue())
        assertThat(value, equalTo("foofoo2"))
        assertThat(error, nullValue())

        val cacheDir = File(tempDir)
        assert(cacheDir.exists())

        cacheDir.deleteRecursively()
        assert(!cacheDir.exists())

        expirableCache.removeAll()

        assert(expirableCache.allKeys().isEmpty())
    }
}

package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.StringDataConvertible
import com.github.kittinunf.fuse.core.build
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.scenario.ExpirableCache
import com.github.kittinunf.fuse.core.scenario.get
import com.github.kittinunf.result.Result
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class FuseScenarioTest : BaseTestCase() {

    @ExperimentalTime
    companion object {
        private val tempDir = createTempDir().absolutePath
        private val cache =
            CacheBuilder.config(tempDir, StringDataConvertible()) {
                callbackExecutor = Executor { it.run() }
            }.build()

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
        expirableCache.remove("hello", Cache.Source.MEM)
        expirableCache.remove("hello", Cache.Source.DISK)

        val lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        expirableCache.get("hello", { "world" }) { (v, e), type ->
            value = v
            error = e
            source = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))
        assertThat(expirableCache.getTimestamp("hello"), not(equalTo(-1L)))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitExpired() {
        var lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        expirableCache.get("hello", { "world" }) { (v, e) ->
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(6000)

        lock = CountDownLatch(1)
        expirableCache.get("hello", { "new world" }, timeLimit = 5.seconds) { (v, e), type ->
            value = v
            error = e
            source = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("new world"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitExpiredButStillForceToUse() {
        var lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        expirableCache.get("expired", { "world" }) { (v, e) ->
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        Thread.sleep(6000)

        lock = CountDownLatch(1)
        expirableCache.get(
            "expired",
            { "new world" },
            timeLimit = 5.seconds,
            useEntryEvenIfExpired = true
        ) { (v, e), type ->
            value = v
            error = e
            source = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.MEM))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitNotExpired() {
        var lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        expirableCache.get("not expired", { "world" }) { (v, e) ->
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        value = null
        error = null

        Thread.sleep(1000)

        lock = CountDownLatch(1)
        expirableCache.get("not expired", { "new world" }, 5.seconds) { (v, e), type ->
            value = v
            error = e
            source = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())
        assertThat(source, not(equalTo(Cache.Source.ORIGIN)))
    }

    @ExperimentalTime
    @Test
    fun fetchWithTimeLimitNotExpiredButNotInMemory() {
        var lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        expirableCache.get("not expired", { "world" }) { (v, e) ->
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        value = null
        error = null

        Thread.sleep(1000)
        expirableCache.remove("not expired", Cache.Source.MEM)

        lock = CountDownLatch(1)
        expirableCache.get("not expired", { "new world" }, 5.seconds) { (v, e), type ->
            value = v
            error = e
            source = type

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())
        assertThat(source, equalTo(Cache.Source.DISK))
    }

    @ExperimentalTime
    @Test
    fun fetchWithFetcherThatCouldFail() {
        var lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        expirableCache.get("can_fail", { "world" }) { (v, e) ->
            value = v
            error = e

            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(value, equalTo("world"))
        assertThat(error, nullValue())

        value = null
        error = null

        lock = CountDownLatch(1)

        val failFetcher = object : Fetcher<String> {
            override val key: String = "can_fail"
            override fun fetch(): Result<String, Exception> = Result.error(Exception("fail catcher"))
        }

        expirableCache.get(failFetcher, Duration.ZERO) { (v, e), s ->
            value = v
            error = e

            source = s
            lock.countDown()
        }
        lock.wait()

        assertThat(value, notNullValue())
        assertThat(error, nullValue())
        assertThat(source, not(equalTo(Cache.Source.ORIGIN)))
    }

    @ExperimentalTime
    @Test
    fun fetchWithFailureFetcherAndAlwaysExpiringEntry() {
        val lock = CountDownLatch(1)

        var value: String? = null
        var error: Exception? = null
        var source: Cache.Source? = null

        val failFetcher = object : Fetcher<String> {
            override val key: String = "will_fail"

            override fun fetch(): Result<String, Exception> = Result.error(Exception("fail catcher"))
        }

        expirableCache.get(failFetcher, Duration.ZERO) { (v, e), s ->
            value = v
            error = e

            source = s
            lock.countDown()
        }
        lock.wait()

        assertThat(value, nullValue())
        assertThat(error, notNullValue())
        assertThat(source, equalTo(Cache.Source.ORIGIN))
    }
}

package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.thread
import com.github.kittinunf.result.Result
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class NetworkFetcher<T : Any>(
    private val url: URL,
    private val convertible: Fuse.DataConvertible<T>
) : Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    private val dispatchedExecutor =
        Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

    private val callbackExecutor = Executor { it.run() }

    override val key: String = url.toString()

    private var cancelled: Boolean = false

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        cancelled = false

        var bytes = ByteArray(0)

        var hasFailed = false

        dispatch(dispatchedExecutor) {
            try {
                val conn = establishConnection(url)
                conn.readTimeout = 15000
                conn.connectTimeout = 15000
                conn.connect()

                val input = conn.inputStream
                bytes = input.readBytes()
            } catch (ex: Exception) {
                hasFailed = true
                thread(callbackExecutor) {
                    handler(Result.error(ex))
                }
            }

            thread(callbackExecutor) {
                if (cancelled or hasFailed) {
                    return@thread
                }

                handler(Result.of(convertFromData(bytes)))
            }
        }
    }

    override fun cancel() {
        cancelled = true
    }

    private fun establishConnection(url: URL): URLConnection {
        return if (url.protocol == "https") {
            val connection = url.openConnection() as HttpsURLConnection
            connection.apply {
                sslSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory()
                hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
            }
        } else {
            url.openConnection() as HttpURLConnection
        }
    }
}

fun <T : Any> Cache<T>.get(url: URL, handler: ((Result<T, Exception>) -> Unit)? = null) {
    get(NetworkFetcher(url, this), handler)
}

fun <T : Any> Cache<T>.get(
    url: URL,
    handler: ((Result<T, Exception>, Cache.Source) -> Unit)? = null
) {
    get(NetworkFetcher(url, this), handler)
}

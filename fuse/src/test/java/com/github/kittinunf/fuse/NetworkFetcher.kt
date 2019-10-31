package com.github.kittinunf.fuse

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

class NetworkFetcher<T : Any>(
    private val url: URL,
    private val convertible: Fuse.DataConvertible<T>
) : Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    override val key: String = url.toString()

    private var cancelled: Boolean = false

    override fun fetch(): Result<T, Exception> {
        val connectResult = Result.of<ByteArray, Exception> {
            val conn = establishConnection(url).apply {
                readTimeout = 15000
                connectTimeout = 15000
            }
            conn.connect()

            val input = conn.inputStream
            input.use { it.readBytes() }
        }

        if (cancelled) return Result.error(RuntimeException("Fetch got cancelled"))

        return connectResult.map { convertFromData(it) }
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

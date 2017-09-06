package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.thread
import com.github.kittinunf.result.Result
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

class NetworkFetcher<out T : Any>(private val url: URL, private val convertible: Fuse.DataConvertible<T>) : Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    override val key: String = url.toString()

    private var cancelled: Boolean = false

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        cancelled = false

        var bytes = ByteArray(0)

        var hasFailed = false

        dispatch(Fuse.dispatchedExecutor) {
            try {
                val conn = establishConnection(url)
                conn.readTimeout = 15000
                conn.connectTimeout = 15000
                conn.connect()

                val input = conn.inputStream
                bytes = input.readBytes()
            } catch(ex: Exception) {
                hasFailed = true
                thread(Fuse.callbackExecutor) {
                    handler(Result.error(ex))
                }
            }

            thread(Fuse.callbackExecutor) {
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

fun <T : Any> Cache<T>.get(url: URL, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>) -> Unit)? = null) {
    get(NetworkFetcher(url, this), configName, handler)
}

fun <T : Any> Cache<T>.get(url: URL, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>, Cache.Type) -> Unit)? = null) {
    get(NetworkFetcher(url, this), configName, handler)
}


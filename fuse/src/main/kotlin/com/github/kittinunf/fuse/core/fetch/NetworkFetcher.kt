package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.mainThread
import com.github.kittinunf.result.Result
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class NetworkFetcher<T : Any>(val url: URL, val convertible: Fuse.DataConvertible<T>) : Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    override val key: String = url.toString()

    var cancelled: Boolean = false

    val backgroundExecutor by lazy { Executors.newSingleThreadExecutor() }

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        cancelled = false

        var bytes = ByteArray(0)

        var hasFailed = false

        dispatch(backgroundExecutor) {
            try {
                val conn = establishConnection(url)
                conn.readTimeout = 15000;
                conn.connectTimeout = 15000;
                conn.connect();

                val input = conn.inputStream;
                bytes = input.readBytes()
            } catch(ex: Exception) {
                mainThread {
                    handler(Result.error(ex))
                }
            }

            mainThread {
                if (cancelled or hasFailed) {
                    return@mainThread
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


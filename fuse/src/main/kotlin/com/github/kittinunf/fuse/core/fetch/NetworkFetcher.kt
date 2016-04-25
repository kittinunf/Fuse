package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.result.Result
import java.net.URL

class NetworkFetcher<T : Any>(val url: URL) : Fetcher<T> {

    override val key: String = url.toURI().rawPath

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        throw UnsupportedOperationException()
    }

}

fun <T : Any> Cache<T>.get(url: URL, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>) -> Unit)? = null) {
    get(NetworkFetcher(url), configName, handler)
}


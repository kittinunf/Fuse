package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.mainThread
import com.github.kittinunf.result.Result
import java.io.File
import java.util.concurrent.Executors

class DiskFetcher<T : Any>(val file: File, val convertible: Fuse.DataConvertible<T>) : Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    override val key: String = file.path

    var cancelled: Boolean = false

    val backgroundExecutor by lazy { Executors.newSingleThreadExecutor() }

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        cancelled = false

        var bytes = ByteArray(0)

        var hasFailed = false

        dispatch(backgroundExecutor) {
            if (cancelled) {
                return@dispatch
            }

            try {
                bytes = file.readBytes()
            } catch(ex: Exception) {
                hasFailed = true
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

}

fun <T : Any> Cache<T>.get(file: File, configName: String = Config.DEFAULT_NAME, handler: ((Result<T, Exception>) -> Unit)? = null) {
    get(DiskFetcher(file, this), configName, handler)
}

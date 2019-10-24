package com.github.kittinunf.fuse.core.fetch

import com.github.kittinunf.fuse.core.Cache
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.util.dispatch
import com.github.kittinunf.fuse.util.thread
import com.github.kittinunf.result.Result
import java.io.File

class DiskFetcher<out T : Any>(val file: File, private val convertible: Fuse.DataConvertible<T>) :
    Fetcher<T>, Fuse.DataConvertible<T> by convertible {

    override val key: String = file.path

    private var cancelled: Boolean = false

    override fun fetch(handler: (Result<T, Exception>) -> Unit) {
        cancelled = false

        var bytes = ByteArray(0)

        var hasFailed = false

        dispatch(Fuse.dispatchedExecutor) {
            if (cancelled) {
                return@dispatch
            }

            try {
                bytes = file.readBytes()
            } catch (ex: Exception) {
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
}

fun <T : Any> Cache<T>.get(
    file: File,
    configName: String = Config.DEFAULT_NAME,
    handler: ((Result<T, Exception>) -> Unit)? = null
) {
    get(DiskFetcher(file, this), configName, handler)
}

fun <T : Any> Cache<T>.get(
    file: File,
    configName: String = Config.DEFAULT_NAME,
    handler: ((Result<T, Exception>, Cache.Type) -> Unit)? = null
) {
    get(DiskFetcher(file, this), configName, handler)
}

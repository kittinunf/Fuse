package com.github.kittinunf.fuse.core

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class Config<T : Any>(
    val cacheDir: String,
    val name: String = DEFAULT_NAME,
    val convertible: Fuse.DataConvertible<T>,
    var diskCapacity: Long = 1024 * 1024 * 20,
    var callbackExecutor: Executor = Executor {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            it.run()
        } else {
            val mainLooperHandler = Handler(Looper.getMainLooper())
            mainLooperHandler.post(it)
        }
    }
) {

    companion object {
        const val DEFAULT_NAME = "FUSE_DEFAULT"
    }

    var transformer: ((key: String, value: T) -> T) = { _, value -> value }
}

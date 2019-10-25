package com.github.kittinunf.fuse.core

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Fuse {

    companion object {

        var dispatchedExecutor =
            Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

        var callbackExecutor = Executor {
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                it.run()
            } else {
                val mainLooperHandler = Handler(Looper.getMainLooper())
                mainLooperHandler.post(it)
            }
        }
    }

    interface DataConvertible<T : Any> {
        fun convertFromData(bytes: ByteArray): T
        fun convertToData(value: T): ByteArray
    }
}

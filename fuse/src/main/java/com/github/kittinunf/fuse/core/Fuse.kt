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

    interface DataConvertible<out T : Any> {
        fun convertFromData(bytes: ByteArray): T
    }

    interface DataRepresentable<in T : Any> {
        fun convertToData(value: T): ByteArray
    }
}

package com.github.kittinunf.fuse.core

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Fuse {

    companion object {

        private lateinit var dir: String

        var dispatchedExecutor: ExecutorService = Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

        var callbackExecutor = Executor {
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                it.run()
            } else {
                val mainLooperHandler = Handler(Looper.getMainLooper())
                mainLooperHandler.post(it)
            }
        }

        val stringCache by lazy { Cache(dir, StringDataConvertible(), StringDataRepresentable()) }
        val bytesCache by lazy { Cache(dir, ByteArrayDataConvertible(), ByteArrayDataRepresentable()) }
        val jsonCache by lazy { Cache(dir, JsonDataConvertible(), JsonDataRepresentable()) }

        fun init(cacheDir: String) {
            dir = cacheDir
        }

    }

    interface DataConvertible<out T : Any> {

        fun convertFromData(bytes: ByteArray): T

    }

    interface DataRepresentable<in T : Any> {

        fun convertToData(value: T): ByteArray

    }

}


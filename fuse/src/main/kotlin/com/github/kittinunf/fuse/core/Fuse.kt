package com.github.kittinunf.fuse.core

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Fuse {

    companion object {

        var backgroundExecutor: ExecutorService = Executors.newScheduledThreadPool(2 * Runtime.getRuntime().availableProcessors())

        lateinit var dir: String

        val stringCache by lazy { Cache(dir, StringDataConvertible(), StringDataRepresentable()) }
        val bytesCache by lazy { Cache(dir, ByteArrayDataConvertible(), ByteArrayDataRepresentable()) }
        val jsonCache by lazy { Cache(dir, JsonDataConvertible(), JsonDataRepresentable()) }

        fun init(cacheDir: String) {
            dir = cacheDir
        }

    }

    interface DataConvertible<T : Any> {

        fun convertFromData(bytes: ByteArray): T

    }

    interface DataRepresentable<T : Any> {

        fun convertToData(value: T): ByteArray

    }

}


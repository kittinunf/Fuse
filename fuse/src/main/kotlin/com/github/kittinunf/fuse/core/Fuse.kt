package com.github.kittinunf.fuse.core

import org.json.JSONObject

class Fuse {

    companion object {

        lateinit var dir: String

        val stringCache by lazy { StringCache(dir) }
        val bytesCache by lazy { ByteArrayCache(dir) }
        val jsonCache by lazy { JsonCache(dir) }

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

class ByteArrayCache(cacheDir: String) : Cache<ByteArray>(cacheDir, ByteArrayDataConvertible(), ByteArrayDataRepresentable())

class StringCache(cacheDir: String) : Cache<String>(cacheDir, StringDataConvertible(), StringDataRepresentable())

class JsonCache(cacheDir: String) : Cache<JSONObject>(cacheDir, JsonDataConvertible(), JsonDataRepresentable())

 

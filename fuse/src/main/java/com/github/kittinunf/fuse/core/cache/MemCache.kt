package com.github.kittinunf.fuse.core.cache

import android.util.LruCache

internal class MemCache {

    private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

    private val cache = object : LruCache<Any, Any>((maxMemory / 8).toInt()) {

        override fun sizeOf(key: Any?, value: Any?): Int {
            return super.sizeOf(key, value)
        }
    }

    operator fun set(key: String, value: Any) {
        cache.put(key, value)
    }

    operator fun get(key: String): Any? = cache.get(key)

    fun remove(key: Any): Boolean = cache.remove(key) != null
}

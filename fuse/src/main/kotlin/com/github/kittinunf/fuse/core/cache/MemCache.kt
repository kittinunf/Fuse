package com.github.kittinunf.fuse.core.cache

import android.util.LruCache

class MemCache {

    private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

    private val cache = LruCache<Any, Any>((maxMemory / 8).toInt())

    operator fun set(key: Any, value: Any) {
        cache.put(key, value)
    }

    operator fun get(key: Any): Any? = cache.get(key)

    fun remove(key: Any): Boolean = cache.remove(key) != null

}
 

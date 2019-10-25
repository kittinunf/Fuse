package com.github.kittinunf.fuse.core.cache

import android.util.LruCache

internal class MemCache : Persistence<Any> {

    private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

    private val cache = object : LruCache<Any, Any>((maxMemory / 8).toInt()) {
    }

    override fun put(key: String, value: Any) {
        cache.put(key, value)
    }

    override fun remove(key: String): Boolean = cache.remove(key) != null

    override fun removeAll() {
        cache.evictAll()
    }

    override fun allKeys(): List<String> = cache.snapshot().keys.map { it.toString() }

    override fun size(): Long = cache.size().toLong()

    override fun get(key: String): Any? = cache.get(key)
}

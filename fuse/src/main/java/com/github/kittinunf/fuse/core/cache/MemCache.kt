package com.github.kittinunf.fuse.core.cache

import android.util.LruCache

internal class MemCache : Persistence<Any> {

    companion object {
        const val KEY_SUFFIX = ".key"
    }

    private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

    private val cache = object : LruCache<String, Any>((maxMemory / 8).toInt()) {
    }

    override fun put(safeKey: String, key: String, value: Any) {
        cache.put(safeKey, value)
        cache.put("$safeKey$KEY_SUFFIX", key)
    }

    override fun remove(key: String): Boolean = cache.remove(key) != null

    override fun removeAll() {
        cache.evictAll()
    }

    override fun allKeys(): Set<String> =
        cache.snapshot().keys.filter { !it.contains(KEY_SUFFIX) }.map { get("$it$KEY_SUFFIX") as String }.toSet()

    override fun size(): Long = cache.size().toLong()

    override fun get(key: String): Any? = cache.get(key)
}

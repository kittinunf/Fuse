package com.github.kittinunf.fuse.core.cache

import android.util.LruCache

internal class MemCache : Persistence<Any> {

    companion object {
        const val KEY_SUFFIX = ".key"
        const val TIME_SUFFIX = ".time"
    }

    private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

    private val cache = object : LruCache<String, Any>((maxMemory / 8).toInt()) {
    }

    override fun put(safeKey: String, key: String, value: Any, timeToPersist: Long) {
        cache.apply {
            put(safeKey, value)
            put(convertKey(safeKey, KEY_SUFFIX), key)
            put(convertKey(safeKey, TIME_SUFFIX), timeToPersist)
        }
    }

    override fun remove(key: String): Boolean {
        val removedValue = cache.remove(key)
        cache.remove(convertKey(key, KEY_SUFFIX))
        cache.remove(convertKey(key, TIME_SUFFIX))
        return removedValue != null
    }

    override fun removeAll() {
        cache.evictAll()
    }

    override fun allKeys(): Set<String> = cache.snapshot()
        .keys.filter { !it.contains(".") }.map { get(convertKey(it, KEY_SUFFIX)) as String }.toSet()

    override fun size(): Long = cache.size().toLong()

    override fun get(key: String): Any? = cache.get(key)

    override fun getTimestamp(key: String): Long? = get(convertKey(key, TIME_SUFFIX)) as? Long

    private fun convertKey(key: String, suffix: String): String = "$key$suffix"
}

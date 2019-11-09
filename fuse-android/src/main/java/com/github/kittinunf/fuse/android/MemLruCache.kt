package com.github.kittinunf.fuse.android

import android.util.LruCache
import com.github.kittinunf.fuse.core.cache.KeyType
import com.github.kittinunf.fuse.core.cache.Persistence

class MemLruCache(private val cache: LruCache<String, Any>) : Persistence<Any> {

    override fun put(safeKey: String, key: String, value: Any, timeToPersist: Long) {
        cache.apply {
            put(safeKey, value)
            put(convertKey(safeKey, KeyType.Key.ordinal.toString()), key)
            put(convertKey(safeKey, KeyType.Time.ordinal.toString()), timeToPersist)
        }
    }

    override fun remove(safeKey: String): Boolean {
        val removedValue = cache.remove(safeKey)
        cache.remove(convertKey(safeKey, KeyType.Key.ordinal.toString()))
        cache.remove(convertKey(safeKey, KeyType.Time.ordinal.toString()))
        return removedValue != null
    }

    override fun removeAll() {
        cache.evictAll()
    }

    override fun allKeys(): Set<String> {
        return cache.snapshot().keys.filter { !it.contains(".") }
            .map { get(convertKey(it, KeyType.Key.ordinal.toString())) as String }
            .toSet()
    }

    override fun size(): Long {
        return cache.size().toLong()
    }

    override fun get(safeKey: String): Any? = cache.get(safeKey)

    override fun getTimestamp(safeKey: String): Long? = get(convertKey(safeKey, KeyType.Time.ordinal.toString())) as? Long

    private fun convertKey(key: String, suffix: String): String = "$key.$suffix"
}

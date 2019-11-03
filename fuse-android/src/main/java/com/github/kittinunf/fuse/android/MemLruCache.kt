package com.github.kittinunf.fuse.android

import android.util.LruCache
import com.github.kittinunf.fuse.core.cache.Persistence

class MemLruCache(val cache: LruCache<String, Any>) : Persistence<Any> {

    companion object {
        const val KEY_SUFFIX = ".key"
        const val TIME_SUFFIX = ".time"
    }

    override fun put(safeKey: String, key: String, value: Any, timeToPersist: Long) {
        cache.apply {
            put(safeKey, value)
            put(convertKey(safeKey, KEY_SUFFIX), key)
            put(convertKey(safeKey, TIME_SUFFIX), timeToPersist)
        }
    }

    override fun remove(safeKey: String): Boolean {
        val removedValue = cache.remove(safeKey)
        cache.remove(convertKey(safeKey, KEY_SUFFIX))
        cache.remove(convertKey(safeKey, TIME_SUFFIX))
        return removedValue != null
    }

    override fun removeAll() {
        cache.evictAll()
    }

    override fun allKeys(): Set<String> = cache.snapshot()
        .keys.filter { !it.contains(".") }.map {
        get(
            convertKey(
                it,
                KEY_SUFFIX
            )
        ) as String
    }.toSet()

    override fun size(): Long = cache.size().toLong()

    override fun get(safeKey: String): Any? = cache.get(safeKey)

    override fun getTimestamp(safeKey: String): Long? = get(
        convertKey(
            safeKey,
            TIME_SUFFIX
        )
    ) as? Long

    private fun convertKey(key: String, suffix: String): String = "$key$suffix"
}

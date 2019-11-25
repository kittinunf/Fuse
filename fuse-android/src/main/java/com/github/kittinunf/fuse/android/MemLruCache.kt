package com.github.kittinunf.fuse.android

import android.util.LruCache
import com.github.kittinunf.fuse.core.cache.Entry
import com.github.kittinunf.fuse.core.cache.Persistence

class MemLruCache(private val cache: LruCache<String, Any>) : Persistence<Any> {

    override fun put(safeKey: String, entry: Entry<Any>) {
        cache.put(safeKey, entry)
    }

    override fun remove(safeKey: String): Boolean = cache.remove(safeKey) != null

    override fun removeAll() {
        cache.evictAll()
    }

    override fun allKeys(): Set<String> {
        return synchronized(this) {
            val snapshot = cache.snapshot()
            snapshot.keys.map { getEntry(it)!!.key }.toSet()
        }
    }

    override fun size(): Long {
        return cache.size().toLong()
    }

    override fun get(safeKey: String): Any? = getEntry(safeKey)?.data

    override fun getTimestamp(safeKey: String): Long? = getEntry(safeKey)?.timestamp

    private fun getEntry(safeKey: String): Entry<Any>? = cache.get(safeKey) as? Entry<Any>
}

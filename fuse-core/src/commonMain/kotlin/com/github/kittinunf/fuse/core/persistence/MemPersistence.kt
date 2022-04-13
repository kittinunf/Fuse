package com.github.kittinunf.fuse.core.persistence

import com.github.kittinunf.fuse.core.model.Entry

internal class MemPersistence : Persistence<Any> {

    private val cache: MutableMap<String, Any> = LinkedHashMap(0, 0.75f)

    override fun put(safeKey: String, entry: Entry<Any>) {
        cache.put(safeKey, entry)
    }

    override fun remove(safeKey: String): Boolean = cache.remove(safeKey) != null

    override fun removeAll() {
        cache.clear()
    }

    override fun allKeys(): Set<String> {
        val snapshot = LinkedHashMap(cache)
        return snapshot.keys
            .mapNotNull { getEntry(it)?.key }
            .toSet()
    }

    override fun size(): Long {
        val snapshot = LinkedHashMap(cache)
        return snapshot.size.toLong()
    }

    override fun get(safeKey: String): Any? = getEntry(safeKey)?.data

    override fun getTimestamp(safeKey: String): Long? = getEntry(safeKey)?.timestamp

    @Suppress("UNCHECKED_CAST")
    private fun getEntry(safeKey: String): Entry<Any>? = cache.get(safeKey) as? Entry<Any>
}

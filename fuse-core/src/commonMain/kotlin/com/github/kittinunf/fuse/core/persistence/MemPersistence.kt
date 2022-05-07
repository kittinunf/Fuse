package com.github.kittinunf.fuse.core.persistence

import com.github.kittinunf.fuse.core.model.Entry

internal class MemPersistence<T : Any> : Persistence<T> {

    private val cache: MutableMap<String, Any> = LinkedHashMap(0, 0.75f)

    override fun put(safeKey: String, entry: Entry<T>) {
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

    override fun get(safeKey: String): T? = getEntry(safeKey)?.data

    override fun getTimestamp(safeKey: String): Long? = getEntry(safeKey)?.timestamp

    @Suppress("UNCHECKED_CAST")
    private fun getEntry(safeKey: String): Entry<T>? = cache.get(safeKey) as? Entry<T>
}

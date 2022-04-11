package com.github.kittinunf.fuse.core.cache

internal class MemCache(val maxSize: Int) : Persistence<Any> {

    val cache: MutableMap<String, Any> = object : LinkedHashMap<String, Any>(0, 0.75f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>?): Boolean = size > maxSize
    }

    override fun put(safeKey: String, entry: Entry<Any>) {
        cache.put(safeKey, entry)
    }

    override fun remove(safeKey: String): Boolean = cache.remove(safeKey) != null

    override fun removeAll() {
        cache.clear()
    }

    override fun allKeys(): Set<String> {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.keys
                .mapNotNull { getEntry(it)?.key }
                .toSet()
        }
    }

    override fun size(): Long {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.size.toLong()
        }
    }

    override fun get(safeKey: String): Any? = getEntry(safeKey)?.data

    override fun getTimestamp(safeKey: String): Long? = getEntry(safeKey)?.timestamp

    @Suppress("UNCHECKED_CAST")
    private fun getEntry(safeKey: String): Entry<Any>? = cache.get(safeKey) as? Entry<Any>
}

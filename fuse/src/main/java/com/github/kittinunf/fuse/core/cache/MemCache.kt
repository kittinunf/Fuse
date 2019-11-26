package com.github.kittinunf.fuse.core.cache

class MemCache(val minimalSize: Int) : Persistence<Any> {

    val cache: MutableMap<String, Any> = object : LinkedHashMap<String, Any>(0, 0.75f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>?): Boolean {
            eldest ?: return false

            if (size > minimalSize) {
                remove(safeKey = eldest.key)
            }
            return false
        }
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

    private fun getEntry(safeKey: String): Entry<Any>? = cache.get(safeKey) as? Entry<Any>
}

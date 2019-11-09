package com.github.kittinunf.fuse.core.cache

class MemCache(val minimalSize: Int) : Persistence<Any> {

    val cache: MutableMap<String, Any> = object : LinkedHashMap<String, Any>(0, 0.75f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>?): Boolean {
            eldest ?: return false

            if ((size / KeyType.values().size) > minimalSize) {
                val keyToRemove = eldest.key.substringBefore(".")
                remove(safeKey = keyToRemove)
            }
            return false
        }
    }

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
        cache.clear()
    }

    override fun allKeys(): Set<String> {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.keys.filter { !it.contains(".") }
                .map { get(convertKey(it, KeyType.Key.ordinal.toString())) as String }.toSet()
        }
    }

    override fun size(): Long {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.size.toLong()
        }
    }

    override fun get(safeKey: String): Any? = cache.get(safeKey)

    override fun getTimestamp(safeKey: String): Long? =
        get(convertKey(safeKey, KeyType.Time.ordinal.toString())) as? Long

    private fun convertKey(key: String, suffix: String): String = "$key.$suffix"
}

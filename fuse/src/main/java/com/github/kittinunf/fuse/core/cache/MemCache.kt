package com.github.kittinunf.fuse.core.cache

internal class MemCache(private val minimalSize: Int = 128) : Persistence<Any> {

    private val cache = object : LinkedHashMap<String, Any>(0, 0.75f, true) {

        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Any>?): Boolean {
            eldest ?: return false

            if ((size / KeyType.values().size) > minimalSize) {
                val keyToRemove = eldest.key.substringBefore(".")
                remove(keyToRemove)
            }
            return false
        }
    }

    override fun put(safeKey: String, key: String, value: Any, timeToPersist: Long) {
        cache.apply {
            put(safeKey, value)
            put(convertKey(safeKey, KeyType.Key.name), key)
            put(convertKey(safeKey, KeyType.Time.name), timeToPersist)
        }
    }

    override fun remove(safeKey: String): Boolean {
        val removedValue = cache.remove(safeKey)
        cache.remove(convertKey(safeKey, KeyType.Key.name))
        cache.remove(convertKey(safeKey, KeyType.Time.name))
        return removedValue != null
    }

    override fun removeAll() {
        cache.clear()
    }

    override fun allKeys(): Set<String> {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.keys.filter { !it.contains(".") }.map { get(convertKey(it, KeyType.Key.name)) as String }.toSet()
        }
    }

    override fun size(): Long {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.size.toLong()
        }
    }

    override fun get(safeKey: String): Any? = cache.get(safeKey)

    override fun getTimestamp(safeKey: String): Long? = get(convertKey(safeKey, KeyType.Time.name)) as? Long

    private fun convertKey(key: String, suffix: String): String = "$key.$suffix"
}

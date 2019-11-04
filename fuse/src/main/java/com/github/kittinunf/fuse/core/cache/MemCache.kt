package com.github.kittinunf.fuse.core.cache

internal class MemCache : Persistence<Any> {

    companion object {
        const val KEY_SUFFIX = ".key"
        const val TIME_SUFFIX = ".time"
    }

    private val cache = LinkedHashMap<String, Any>(0, 0.75f, true)

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
        cache.clear()
    }

    override fun allKeys(): Set<String> {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.keys.filter { !it.contains(".") }.map { get(convertKey(it, KEY_SUFFIX)) as String }.toSet()
        }
    }

    override fun size(): Long {
        return synchronized(this) {
            val snapshot = LinkedHashMap(cache)
            snapshot.size.toLong()
        }
    }

    override fun get(safeKey: String): Any? = cache.get(safeKey)

    override fun getTimestamp(safeKey: String): Long? = get(convertKey(safeKey, TIME_SUFFIX)) as? Long

    private fun convertKey(key: String, suffix: String): String = "$key$suffix"
}

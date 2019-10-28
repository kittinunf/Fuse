package com.github.kittinunf.fuse.core.cache

import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.nio.charset.Charset

internal class DiskCache private constructor(private val cache: DiskLruCache) :
    Persistence<ByteArray> {

    enum class OutputStreamIndex {
        Data, Key
    }

    companion object {
        const val JOURNAL_FILE = "journal"

        fun open(cacheDir: String, uniqueName: String, capacity: Long): DiskCache {
            val f = File(cacheDir)
            val disk = DiskLruCache.open(
                f.resolve(uniqueName),
                1,
                OutputStreamIndex.values().size,
                capacity
            )
            return DiskCache(disk)
        }
    }

    override fun put(safeKey: String, key: String, value: ByteArray) {
        cache.edit(safeKey).apply {
            newOutputStream(0).use { it.write(value) }
            newOutputStream(1).use { it.write(key.toByteArray()) }
            commit()
        }
    }

    override fun remove(key: String) = cache.remove(key)

    override fun removeAll() {
        cache.delete()
    }

    override fun allKeys(): Set<String> {
        return allSafeKeys()
            .map {
                get(it, OutputStreamIndex.Key.ordinal)!!.toString(Charset.defaultCharset())
            }
            .toSet()
    }

    private fun allSafeKeys() = synchronized(this) {
        cache.directory.listFiles().filter { it.isFile && it.name != JOURNAL_FILE }
            .map { it.name.substringBefore(".") }
    }

    override fun size(): Long = cache.size()

    override fun get(key: String): ByteArray? = get(key, OutputStreamIndex.Data.ordinal)

    private fun get(key: String, indexStream: Int): ByteArray? {
        val snapshot = cache.get(key)
        return snapshot?.let {
            it.getInputStream(indexStream).use { it.readBytes() }
        }
    }

    fun setIfMissing(safeKey: String, key: String, value: ByteArray) {
        val fetched = get(safeKey)
        if (fetched == null) {
            put(safeKey, key, value)
        }
    }
}

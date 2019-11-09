package com.github.kittinunf.fuse.core.cache

import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.nio.charset.Charset

internal class DiskCache private constructor(private val cache: DiskLruCache) : Persistence<ByteArray> {

    companion object {
        const val JOURNAL_FILE = "journal"

        fun open(cacheDir: String, uniqueName: String, capacity: Long): DiskCache {
            val dir = File(cacheDir)
            val disk = DiskLruCache.open(
                dir.resolve(uniqueName),
                1,
                KeyType.values().size,
                capacity
            )
            return DiskCache(disk)
        }
    }

    override fun put(safeKey: String, key: String, value: ByteArray, timeToPersist: Long) {
        cache.edit(safeKey).apply {
            newOutputStream(KeyType.Data.ordinal).use { it.write(value) }
            newOutputStream(KeyType.Key.ordinal).use { it.write(key.toByteArray()) }
            newOutputStream(KeyType.Time.ordinal).use { it.write(timeToPersist.toString().toByteArray()) }
            commit()
        }
    }

    override fun remove(safeKey: String) = cache.remove(safeKey)

    override fun removeAll() {
        cache.delete()
    }

    override fun allKeys(): Set<String> = allSafeKeys()
        .map { get(it, KeyType.Key.ordinal)!!.toString(Charset.defaultCharset()) }
        .toSet()

    private fun allSafeKeys() = synchronized(this) {
        cache.directory.listFiles().filter { it.isFile && it.name != JOURNAL_FILE }.map { it.name.substringBefore(".") }
    }

    override fun size(): Long = cache.size()

    override fun get(safeKey: String): ByteArray? = get(safeKey, KeyType.Data.ordinal)

    override fun getTimestamp(safeKey: String): Long? =
        get(safeKey, KeyType.Time.ordinal)?.toString(Charset.defaultCharset())?.toLong()

    private fun get(key: String, indexStream: Int): ByteArray? {
        val snapshot = cache.get(key)
        return snapshot?.let {
            it.getInputStream(indexStream).use { it.readBytes() }
        }
    }
}

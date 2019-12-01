package com.github.kittinunf.fuse.core.cache

import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.nio.charset.Charset
import kotlinx.serialization.internal.ByteArraySerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

internal class DiskCache private constructor(private val cache: DiskLruCache) : Persistence<ByteArray> {

    companion object {
        const val JOURNAL_FILE = "journal"

        fun open(cacheDir: String, uniqueName: String, capacity: Long): DiskCache {
            val dir = File(cacheDir)
            val disk = DiskLruCache.open(dir.resolve(uniqueName), 1, 1, capacity)
            return DiskCache(disk)
        }
    }

    private val json = Json(JsonConfiguration.Default)

    override fun put(safeKey: String, entry: Entry<ByteArray>) {
        cache.edit(safeKey).apply {
            newOutputStream(0).use {
                val serialized = json.stringify(Entry.serializer(ByteArraySerializer), entry)
                it.write(serialized.toByteArray())
            }
            commit()
        }
    }

    override fun remove(safeKey: String) = cache.remove(safeKey)

    override fun removeAll() {
        allSafeKeys().forEach { cache.remove(it) }
    }

    override fun allKeys(): Set<String> = allSafeKeys()
        .mapNotNull { getEntry(it)?.key }
        .toSet()

    private fun allSafeKeys() = synchronized(this) {
        cache.directory.listFiles().filter { it.isFile && it.name != JOURNAL_FILE }.map { it.name.substringBefore(".") }
    }

    override fun size(): Long = cache.size()

    override fun get(safeKey: String): ByteArray? = getEntry(safeKey)?.data

    override fun getTimestamp(safeKey: String): Long? = getEntry(safeKey)?.timestamp

    private fun getEntry(safeKey: String): Entry<ByteArray>? {
        val bytes = cache.get(safeKey)?.use { snapshot ->
            snapshot.getInputStream(0).use { it.readBytes() }
        }
        return bytes?.toString(Charset.defaultCharset())?.let {
            json.parse(Entry.serializer(ByteArraySerializer), it)
        }
    }
}

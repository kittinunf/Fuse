package com.github.kittinunf.fuse.core.cache

import com.jakewharton.disklrucache.DiskLruCache
import java.io.File

internal class DiskCache private constructor(private val cache: DiskLruCache) {

    companion object {
        fun open(cacheDir: String, uniqueName: String, capacity: Long): DiskCache {
            val f = File(cacheDir)
            val disk = DiskLruCache.open(f.resolve(uniqueName), 1, 1, capacity)
            return DiskCache(disk)
        }
    }

    operator fun set(key: String, value: ByteArray) {
        cache.edit(key).apply {
            newOutputStream(0).use { it.write(value) }
            commit()
        }
    }

    operator fun get(key: String): ByteArray? {
        val snapshot = cache.get(key)
        return snapshot?.let {
            it.getInputStream(0).use { it.readBytes() }
        }
    }

    fun setIfMissing(key: String, value: ByteArray) {
        val fetched = get(key)
        if (fetched == null) {
            set(key, value)
        }
    }

    fun remove(key: Any): Boolean = cache.remove(key.toString())
}

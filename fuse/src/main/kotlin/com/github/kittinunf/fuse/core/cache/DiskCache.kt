package com.github.kittinunf.fuse.core.cache

import com.jakewharton.disklrucache.DiskLruCache
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File

class DiskCache private constructor(private val cache: DiskLruCache) {

    companion object {
        fun open(cacheDir: String, uniqueName: String, capacity: Long): DiskCache {
            val f = File(cacheDir)
            val disk = DiskLruCache.open(f.resolve(uniqueName), 1, 1, capacity)
            return DiskCache(disk)
        }
    }

    operator fun set(key: Any, value: ByteArray) {
        val editor = cache.edit(key.toString())
        BufferedOutputStream(editor.newOutputStream(0)).use {
            it.write(value)
        }
        editor.commit()
    }

    operator fun get(key: Any): ByteArray? {
        val snapshot = cache.get(key.toString())
        return snapshot?.let {
            BufferedInputStream(it.getInputStream(0)).use {
                it.readBytes()
            }
        }
    }

    fun moveToHead(key: Any) {
        cache.get(key.toString())
    }

    fun remove(key: Any) {
        cache.remove(key.toString())
    }

}

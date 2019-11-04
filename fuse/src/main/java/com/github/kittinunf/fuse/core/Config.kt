package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.cache.DiskCache
import com.github.kittinunf.fuse.core.cache.MemCache
import com.github.kittinunf.fuse.core.cache.Persistence

class Config<T : Any>(
    val cacheDir: String,
    val name: String = DEFAULT_NAME,
    val convertible: Fuse.DataConvertible<T>,
    var diskCapacity: Long = 1024 * 1024 * 20,
    var memCache: Persistence<Any> = defaultMemoryCache(),
    var diskCache: Persistence<ByteArray> = defaultDiskCache(cacheDir, name, diskCapacity)
) {

    companion object {
        const val DEFAULT_NAME = "FUSE_DEFAULT"
    }

    var transformer: ((key: String, value: T) -> T) = { _, value -> value }
}

internal fun defaultMemoryCache(): Persistence<Any> = MemCache()
internal fun defaultDiskCache(cacheDir: String, name: String, diskCapacity: Long): Persistence<ByteArray> =
    DiskCache.open(
        cacheDir,
        name,
        diskCapacity
    )

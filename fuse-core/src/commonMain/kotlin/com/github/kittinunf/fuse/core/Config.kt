package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.MemPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence

private const val DEFAULT_NAME = "com.github.kittinunf.fuse"

class Config<T : Any>(
    val name: String = DEFAULT_NAME,
    val cacheDir: String,
    val convertible: Fuse.DataConvertible<T>,
    var diskCapacity: Long = 1024 * 1024 * 20,
    var memCache: Persistence<Any> = MemPersistence(),
    var diskCache: Persistence<ByteArray>
) {
    var transformer: ((key: String, value: T) -> T) = { _, value -> value }
}

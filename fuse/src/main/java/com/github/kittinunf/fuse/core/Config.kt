package com.github.kittinunf.fuse.core

class Config<T : Any>(
    val cacheDir: String,
    val name: String = DEFAULT_NAME,
    val convertible: Fuse.DataConvertible<T>,
    var diskCapacity: Long = 1024 * 1024 * 20
) {

    companion object {
        const val DEFAULT_NAME = "FUSE_DEFAULT"
    }

    var transformer: ((key: String, value: T) -> T) = { _, value -> value }
}

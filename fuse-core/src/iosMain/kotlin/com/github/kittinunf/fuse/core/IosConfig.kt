package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.formatter.JsonBinaryFormatter
import com.github.kittinunf.fuse.core.persistence.IosDiskPersistence
import com.github.kittinunf.fuse.core.persistence.MemPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import platform.Foundation.NSURL

class IosConfig<T : Any>(
    override val name: String,
    val path: NSURL? = null,
    override val formatter: BinaryFormat = JsonBinaryFormatter(),
    override val serializer: KSerializer<T>,
    override val diskCapacity: Long = 1024 * 1024 * 20,
    override val transformer: (key: String, value: T) -> T = { _, value -> value }
) : Config<T> {

    override val memCache: Persistence<T> = MemPersistence()

    override val diskCache: Persistence<ByteArray> = IosDiskPersistence(name, path, formatter)
}

package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.IosDiskPersistence
import com.github.kittinunf.fuse.core.persistence.MemPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import platform.Foundation.NSURL

class IosConfig<T : Any>(
    override val name: String,
    val path: NSURL? = null,
    override val serializer: KSerializer<T>,
    override val diskCapacity: Long,
    override val transformer: (key: String, value: T) -> T = { _, value -> value }
) : Config<T> {

    override val formatDriver: BinaryFormat
        get() = TODO("Not yet implemented")

    override val memCache: Persistence<T> = MemPersistence()

    override val diskCache: Persistence<ByteArray> = IosDiskPersistence(name, path, formatDriver)
}

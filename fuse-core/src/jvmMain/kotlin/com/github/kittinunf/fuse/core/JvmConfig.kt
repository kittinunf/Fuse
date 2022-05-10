package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.formatter.BinarySerializer
import com.github.kittinunf.fuse.core.persistence.JvmDiskPersistence
import com.github.kittinunf.fuse.core.persistence.MemPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import java.io.File

class JvmConfig<T : Any>(
    override val name: String,
    val path: File? = null,
    override val serializer: KSerializer<T>,
    override val diskCapacity: Long = 1024 * 1024 * 20,
    override val transformer: (key: String, value: T) -> T = { _, value -> value }
) : Config<T> {

    override val formatter: BinaryFormat = BinarySerializer()

    override val memCache: Persistence<T> = MemPersistence()

    override val diskCache: Persistence<ByteArray> = JvmDiskPersistence(name, path, formatter)
}

package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer

interface Config<T : Any> {
    val name: String
    val formatDriver: BinaryFormat
    val diskCapacity: Long
    val serializer: KSerializer<T>
    val memCache: Persistence<T>
    val diskCache: Persistence<ByteArray>
    val transformer: ((key: String, value: T) -> T)
}

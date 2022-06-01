package com.github.kittinunf.fuse.core

import android.content.Context
import com.github.kittinunf.fuse.core.formatter.JsonBinaryConverter
import com.github.kittinunf.fuse.core.persistence.JvmDiskPersistence
import com.github.kittinunf.fuse.core.persistence.MemPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer

fun <T : Any> AndroidConfig(
    name: String,
    context: Context,
    serializer: KSerializer<T>,
    formatter: BinaryFormat = JsonBinaryConverter(),
    diskCapacity: Long = 1024 * 1024 * 20,
    transformer: (key: String, value: T) -> T = { _, value -> value },
    memCache: Persistence<T> = MemPersistence(),
    diskCache: Persistence<ByteArray> = JvmDiskPersistence(name, context.cacheDir)
) = JvmConfig(
    name,
    path = context.cacheDir,
    serializer = serializer,
    formatter = formatter,
    diskCapacity = diskCapacity,
    transformer = transformer,
    memCache = memCache,
    diskCache = diskCache
)

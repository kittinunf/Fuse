package com.github.kittinunf.fuse.core

import android.content.Context
import kotlinx.serialization.KSerializer

fun <T : Any> AndroidConfig(
    name: String,
    context: Context,
    serializer: KSerializer<T>,
    diskCapacity: Long = 1024 * 1024 * 20,
    transformer: (key: String, value: T) -> T = { _, value -> value }
) = JvmConfig(name, context.cacheDir, serializer, diskCapacity, transformer)

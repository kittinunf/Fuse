package com.github.kittinunf.fuse.core

import android.content.Context
import kotlinx.serialization.builtins.ByteArraySerializer

internal actual fun createByteTestCache(name: String, context: Any): Cache<ByteArray> {
    val context = context as Context
    return JvmConfig(name, path = context.cacheDir, serializer = ByteArraySerializer()).build()
}

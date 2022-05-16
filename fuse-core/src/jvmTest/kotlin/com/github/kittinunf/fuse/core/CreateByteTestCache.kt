package com.github.kittinunf.fuse.core

import kotlinx.serialization.builtins.ByteArraySerializer

internal actual fun createByteTestCache(name: String, context: Any): Cache<ByteArray> {
    return JvmConfig(name, path = createTempDir(suffix = "").parentFile, serializer = ByteArraySerializer()).build()
}

package com.github.kittinunf.fuse.core

import kotlinx.serialization.builtins.serializer

internal actual fun createStringTestCache(name: String, context: Any): Cache<String> {
    return JvmConfig(name, path = createTempDir(suffix = "").parentFile, serializer = String.serializer()).build()
}

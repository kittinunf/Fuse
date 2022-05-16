package com.github.kittinunf.fuse.core

import kotlinx.serialization.builtins.serializer

internal actual fun createStringTestCache(name: String, context: Any): Cache<String> {
    return IosConfig(name, path = null, serializer = String.serializer()).build()
}

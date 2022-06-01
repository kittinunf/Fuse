package com.github.kittinunf.fuse.core

import kotlinx.serialization.builtins.serializer
import com.github.kittinunf.fuse.core.model.Product

internal actual fun createJsonTestCache(name: String, context: Any): Cache<Product> {
    return JvmConfig(name, path = createTempDir(suffix = "").parentFile, serializer = Product.serializer()).build()
}

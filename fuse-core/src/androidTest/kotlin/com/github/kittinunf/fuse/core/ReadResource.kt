package com.github.kittinunf.fuse.core

actual fun readResource(name: String): ByteArray {
    return ClassLoader.getSystemResourceAsStream(name).readBytes()
}

package com.github.kittinunf.fuse.core

interface IO {
    val path: String

    fun readAsByte(): ByteArray
}

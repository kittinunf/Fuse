package com.github.kittinunf.fuse.core

import java.security.MessageDigest

internal actual fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") { String.format("%02x", it) }
}

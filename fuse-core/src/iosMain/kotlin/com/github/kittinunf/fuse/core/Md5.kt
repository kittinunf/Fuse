package com.github.kittinunf.fuse.core

import kotlinx.cinterop.refTo
import platform.CoreCrypto.CC_MD5
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringWithFormat

@OptIn(ExperimentalUnsignedTypes::class)
internal actual fun String.md5(): String {
    val data = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
    val hash = UByteArray(CC_MD5_DIGEST_LENGTH) { 0u }
    CC_MD5(data!!.bytes, length().toUInt(), hash.refTo(0))
    return hash.joinToString("") { NSString.stringWithFormat("%02hhx", it) }
}

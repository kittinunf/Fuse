package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.toByteArray
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile

actual fun readResource(name: String): ByteArray {
    val paths = name.split("[.|/]".toRegex())
    val path = NSBundle.mainBundle.pathForResource("resources/${paths[2]}", ofType = paths[3])
    val data = NSData.dataWithContentsOfFile(path!!)
    return data?.toByteArray() ?: ByteArray(0)
}

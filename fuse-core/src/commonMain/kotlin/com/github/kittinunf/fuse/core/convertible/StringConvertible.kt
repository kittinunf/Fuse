package com.github.kittinunf.fuse.core.convertible

import com.github.kittinunf.fuse.core.Fuse

class StringDataConvertible : Fuse.DataConvertible<String> {

    override fun convertFromData(bytes: ByteArray): String = bytes.decodeToString()

    override fun convertToData(value: String): ByteArray = value.encodeToByteArray()
}

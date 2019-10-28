package com.github.kittinunf.fuse.core

import java.nio.charset.Charset

class ByteArrayDataConvertible : Fuse.DataConvertible<ByteArray> {
    override fun convertFromData(bytes: ByteArray): ByteArray = bytes
    override fun convertToData(value: ByteArray): ByteArray = value
}

class StringDataConvertible(private val charset: Charset = Charset.defaultCharset()) : Fuse.DataConvertible<String> {
    override fun convertFromData(bytes: ByteArray): String = bytes.toString(charset)
    override fun convertToData(value: String): ByteArray = value.toByteArray(charset)
}

package com.github.kittinunf.fuse.core

import org.json.JSONObject
import java.nio.charset.Charset

class ByteArrayDataConvertible : Fuse.DataConvertible<ByteArray> {

    override fun convertFromData(bytes: ByteArray): ByteArray = bytes

}

class ByteArrayDataRepresentable : Fuse.DataRepresentable<ByteArray> {

    override fun convertToData(value: ByteArray): ByteArray = value
}

class StringDataConvertible(val charset: Charset = Charset.defaultCharset()) : Fuse.DataConvertible<String> {

    override fun convertFromData(bytes: ByteArray): String = bytes.toString(charset)

}

class StringDataRepresentable(val charset: Charset = Charset.defaultCharset()) : Fuse.DataRepresentable<String> {

    override fun convertToData(value: String): ByteArray = value.toByteArray(charset)

}

class JsonDataConvertible(val charset: Charset = Charset.defaultCharset()) : Fuse.DataConvertible<JSONObject> {

    override fun convertFromData(bytes: ByteArray): JSONObject = JSONObject(bytes.toString(charset))

}

class JsonDataRepresentable(val charset: Charset = Charset.defaultCharset()) : Fuse.DataRepresentable<JSONObject> {

    override fun convertToData(value: JSONObject): ByteArray = value.toString().toByteArray(charset)

}

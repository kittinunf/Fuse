package com.github.kittinunf.fuse.core.formatter

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

internal class JsonBinaryConverter(private val json: Json = Json) : BinaryFormat {

    override val serializersModule: SerializersModule = json.serializersModule

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T =
        json.decodeFromString(deserializer, bytes.decodeToString())

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray =
        json.encodeToString(serializer, value).encodeToByteArray()
}

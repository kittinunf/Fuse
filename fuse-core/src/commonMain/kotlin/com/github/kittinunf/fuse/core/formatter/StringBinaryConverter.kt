package com.github.kittinunf.fuse.core.formatter

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

internal class StringBinaryConverter : BinaryFormat {

    override val serializersModule: SerializersModule = EmptySerializersModule

    override fun <T> decodeFromByteArray(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T = bytes.decodeToString() as T

    override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray = (value as String).encodeToByteArray()
}

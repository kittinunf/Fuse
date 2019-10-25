package com.github.kittinunf.fuse.core

class Fuse {

    interface DataConvertible<T : Any> {
        fun convertFromData(bytes: ByteArray): T
        fun convertToData(value: T): ByteArray
    }
}

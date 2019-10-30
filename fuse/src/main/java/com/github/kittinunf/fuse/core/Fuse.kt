package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.result.Result

class Fuse {

    interface DataConvertible<T : Any> {
        fun convertFromData(bytes: ByteArray): T
        fun convertToData(value: T): ByteArray
    }

    interface Cacheable {

        interface Put<T : Any> {

            fun put(fetcher: Fetcher<T>, handler: ((Result<T, Exception>) -> Unit)? = null)
        }

        interface Get<T : Any> {

            fun get(fetcher: Fetcher<T>, handler: ((Result<T, Exception>) -> Unit)? = null)

            fun get(
                fetcher: Fetcher<T>,
                handler: ((Result<T, Exception>, Cache.Source) -> Unit)? = null
            )
        }

        fun remove(key: String, removeOnlyInMemory: Boolean = false)

        fun removeAll(removeOnlyInMemory: Boolean = false)

        fun allKeys(): Set<String>

        fun getTimestamp(key: String): Long
    }
}

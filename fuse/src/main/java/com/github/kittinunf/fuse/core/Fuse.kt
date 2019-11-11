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

            /**
             *  Put the entry supplied by fetcher into the persistence.
             *  This method will automatically fetch the value from the fetcher and put the entry into the cache (both Memory, and Disk).
             *
             * @param fetcher The fetcher object that can be used to fetch the new value from the origin
             * @return Result<T, Exception> The Result that represents the success/failure of the operation
             */
            fun put(fetcher: Fetcher<T>): Result<T, Exception>
        }

        interface Get<T : Any> {

            /**
             *  Get the entry associated with its particular key which provided by the persistence.
             *  This method will automatically fetch if and only if the entry was not already saved in the persistence previously
             *  Otherwise it will return the entry from the persistence
             *
             * @param fetcher The fetcher object that can be used to fetch the new value from the origin
             * @return Result<T, Exception> The Result that represents the success/failure of the operation
             */
            fun get(fetcher: Fetcher<T>): Result<T, Exception>

            /**
             *  Get the entry associated with its particular key which provided by the persistence.
             *  This method will automatically fetch if and only if the entry was not already saved in the persistence previously
             *  Otherwise it will return the entry from the persistence which specified by Source (ORIGIN, MEM, or DISK)
             *
             * @param fetcher The fetcher object that can be used to fetch the new value from the origin
             * @return Pair<Result<T, Exception>, Cache.Source> The Pair of the result that represents the success/failure of the operation and The source of the entry
             */
            fun getWithSource(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Cache.Source>
        }

        /**
         *  Remove the entry associated with its particular key which was saved previously
         *  In the case of, entry is not found, it will no-op and return false
         *
         * @param key The key associated with the object to be persisted
         * @param fromSource The source of the value to be removed, either MEM or DISK
         * @return Boolean Whether the value was removed successfully
         */
        fun remove(key: String, fromSource: Cache.Source = Cache.Source.MEM): Boolean

        /**
         *  Remove all the entry in the persistence
         */
        fun removeAll()

        /**
         *  Retrieve the keys from all values persisted
         * @return Set<String> Set of keys
         */
        fun allKeys(): Set<String>

        /**
         *  Check whether the entry for the given key is there in the persistence or not
         * @return Boolean The result of the check, true if the entry is there otherwise false
         */
        fun hasKey(key: String): Boolean

        /**
         *  Retrieve the keys from all values persisted
         * @param key The key associated with the object to be persisted
         * @return Long represents the timestamp in milliseconds since epoch 1970
         */
        fun getTimestamp(key: String): Long
    }
}

package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetch.DiskFetcher
import com.github.kittinunf.fuse.core.fetch.Fetcher
import com.github.kittinunf.fuse.core.fetch.NeverFetcher
import com.github.kittinunf.fuse.core.fetch.SimpleFetcher
import com.github.kittinunf.result.Result
import java.io.File

object Fuse {

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
            fun getWithSource(fetcher: Fetcher<T>): Pair<Result<T, Exception>, Source>
        }

        /**
         *  Remove the entry associated with its particular key which was saved previously
         *  In the case of, entry is not found, it will no-op and return false
         *
         * @param key The key associated with the object to be persisted
         * @param fromSource The source of the value to be removed, either MEM or DISK
         * @return Boolean Whether the value was removed successfully
         */
        fun remove(key: String, fromSource: Source = Source.MEM): Boolean

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

// region File
/**
 *  Get the entry associated as a Data of file content in T with its particular key as File path. If File is not there or too large, it returns as [Result.Failure]
 *  Otherwise, it returns [Result.Success] of data of a given file in T
 *
 * @param file The file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.get(file: File): Result<T, Exception> = get(DiskFetcher(file, this))

/**
 *  Get the entry associated as a Data of file content in T with its particular key as File path. If File is not there or too large, it returns as [Result.Failure]
 *  Otherwise, it returns [Result.Success] data of a given file in T
 *
 * @param file The file object that represent file data on the disk
 * @return Pair<Result<T, Exception>, Source>> The Result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.getWithSource(file: File): Pair<Result<T, Exception>, Source> =
    getWithSource(DiskFetcher(file, this))

/**
 *  Put the entry as a content of a file into Cache
 *
 * @param file The file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.put(file: File): Result<T, Exception> = put(DiskFetcher(file, this))
// endregion File

// region Value
/**
 *  Get the entry associated as a value in T by using lambda getValue as a default value generator. If value for associated Key is not there, it saves with value from defaultValue.
 *
 * @param key The String represent key of the entry
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.get(key: String, defaultValue: (() -> T?)): Result<T, Exception> {
    val fetcher = SimpleFetcher(key, defaultValue)
    return get(fetcher)
}

/**
 *  Get the entry associated as a value in T. Unlike [Cache<T>.get(key: String, defaultValue: (() -> T))] counterpart, if value for associated Key is not there, it returns as [Result.Failure]
 *
 * @param key The String represent key of the entry
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.get(key: String): Result<T, Exception> = get(NeverFetcher(key))

/**
 *  Get the entry associated as a value in T by using lambda as a default value generator. if value for associated key is not there, it saves with value from defaultValue.
 *
 * @param key The string represent key of the entry
 * @return Pair<Result<T, Exception>, Source>> The result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.getWithSource(key: String, getValue: (() -> T?)): Pair<Result<T, Exception>, Source> {
    val fetcher = SimpleFetcher(key, getValue)
    return getWithSource(fetcher)
}

/**
 *  Get the entry associated as a value in T by using lambda as a default value generator. if value for associated key is not there, it saves with value from defaultValue.
 *
 * @param key The string represent key of the entry
 * @return Pair<Result<T, Exception>, Source>> The result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.getWithSource(key: String): Pair<Result<T, Exception>, Source> = getWithSource(NeverFetcher(key))

/**
 *  Put the entry as a content of a file into Cache
 *
 * @param key file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
fun <T : Any> Cache<T>.put(key: String, putValue: T): Result<T, Exception> {
    val fetcher = SimpleFetcher(key, { putValue })
    return put(fetcher)
}
// endregion Value


package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetcher.JvmDiskFetcher
import com.github.kittinunf.result.Result
import kotlinx.serialization.serializer
import java.io.File

/**
 *  Get the entry associated as a Data of file content in T with its particular key as File path in [File] format. If File is not there or too large, it returns as [Result.Failure]
 *  Otherwise, it returns [Result.Success] of data of a given file in T
 *
 * @param file The file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
inline fun <reified T : Any> Cache<T>.get(file: File): Result<T, Exception> =
    get(JvmDiskFetcher(file, binaryFormat = this, serializer = serializersModule.serializer()))

package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetcher.IosDiskFetcher
import com.github.kittinunf.result.Result
import kotlinx.serialization.serializer
import platform.Foundation.NSURL

/**
 *  Get the entry associated as a Data of file content in T with its particular key as File path in [NSURL] format. If File is not there or too large, it returns as [Result.Failure]
 *  Otherwise, it returns [Result.Success] of data of a given file in T
 *
 * @param file The file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
inline fun <reified T : Any> Cache<T>.get(url: NSURL): Result<T, Exception> =
    get(IosDiskFetcher(url, binaryFormat = this, serializer = serializersModule.serializer()))

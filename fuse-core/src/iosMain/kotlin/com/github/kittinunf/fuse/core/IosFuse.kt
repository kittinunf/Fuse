package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetcher.IosDiskFetcher
import com.github.kittinunf.result.Result
import kotlinx.serialization.serializer
import platform.Foundation.NSURL

/**
 *  Get the entry associated as a content of a File in T with its particular key as path in [NSURL] format. If File is not there or too large, it returns as [Result.Failure]
 *  Otherwise, it returns [Result.Success] of data of a given file in T
 *
 * @param file The file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
inline fun <reified T : Any> Cache<T>.get(url: NSURL): Result<T, Exception> =
    get(IosDiskFetcher(url, formatter = this, serializer = serializersModule.serializer()))

/**
 *  Put the entry as a content of a in T with its key as path in [NSURL] format into Cache
 *
 * @param file The file object that represent file data on the disk
 * @return Result<T, Exception> The Result that represents the success/failure of the operation
 */
inline fun <reified T : Any> Cache<T>.put(url: NSURL): Result<T, Exception> =
    put(IosDiskFetcher(url, formatter = this, serializer = serializersModule.serializer()))

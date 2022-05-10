package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.fetcher.JvmDiskFetcher
import com.github.kittinunf.result.Result
import kotlinx.serialization.serializer
import java.io.File

inline fun <reified T : Any> Cache<T>.get(file: File): Result<T, Exception> =
    get(JvmDiskFetcher(file.absolutePath, file.inputStream(), serializersModule.serializer()))

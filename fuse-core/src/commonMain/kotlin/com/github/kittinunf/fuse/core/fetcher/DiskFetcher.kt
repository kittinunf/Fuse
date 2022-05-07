package com.github.kittinunf.fuse.core.fetcher

import com.github.kittinunf.fuse.core.Fuse
import com.github.kittinunf.fuse.core.IO
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.map

//internal class DiskFetcher<T : Any>(private val file: IO, private val convertible: Fuse.DataConvertible<T>) : Fetcher<T>,
//    Fuse.DataConvertible<T> by convertible {
//
//    override val key: String = file.path
//
//    private var cancelled: Boolean = false
//
//    override fun fetch(): Result<T, Exception> {
//        val result = Result.of<ByteArray, Exception> { file.readAsByte() }
//        if (cancelled) return Result.failure(RuntimeException("Fetch got cancelled"))
//        return result.map { convertFromData(it) }
//    }
//
//    override fun cancel() {
//        cancelled = true
//    }
//}

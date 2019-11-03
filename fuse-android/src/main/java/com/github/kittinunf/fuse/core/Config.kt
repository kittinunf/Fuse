package com.github.kittinunf.fuse.core

fun <T : Any> Config<T>.defaultAndroidMemoryCache() = MemLruCache()

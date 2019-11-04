package com.github.kittinunf.fuse.android

import android.util.LruCache
import com.github.kittinunf.fuse.core.Config

private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

fun <T : Any> Config<T>.defaultAndroidMemoryCache(cache: LruCache<String, Any> = LruCache((maxMemory / 8).toInt())) =
    MemLruCache(cache)

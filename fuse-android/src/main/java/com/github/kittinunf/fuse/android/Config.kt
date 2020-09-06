package com.github.kittinunf.fuse.android

import android.util.LruCache

private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

fun defaultAndroidMemoryCache(cache: LruCache<String, Any> = LruCache((maxMemory / 8).toInt())) =
    MemLruCache(cache)

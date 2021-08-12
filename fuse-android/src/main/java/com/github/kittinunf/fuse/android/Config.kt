package com.github.kittinunf.fuse.android

import android.content.Context
import android.util.LruCache
import com.github.kittinunf.fuse.core.CacheBuilder
import com.github.kittinunf.fuse.core.Config
import com.github.kittinunf.fuse.core.Fuse

private val maxMemory = Runtime.getRuntime().maxMemory() / 1024

private const val defaultCacheName = "com.github.kittinunf.fuse"

fun <T : Any> CacheBuilder.config(
    context: Context,
    dir: String = context.cacheDir.path,
    name: String = defaultCacheName,
    convertible: Fuse.DataConvertible<T>,
    construct: Config<T>.() -> Unit = {}
): Config<T> = Config(dir, name, convertible).apply(construct)

fun defaultAndroidMemoryCache(cache: LruCache<String, Any> = LruCache((maxMemory / 8).toInt())) =
    MemLruCache(cache)

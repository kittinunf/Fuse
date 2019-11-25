package com.github.kittinunf.fuse.core.cache

import kotlinx.serialization.Serializable

@Serializable
data class Entry<T : Any>(val key: String, val data: T, val timestamp: Long)
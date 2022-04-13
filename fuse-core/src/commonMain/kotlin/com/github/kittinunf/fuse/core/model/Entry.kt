package com.github.kittinunf.fuse.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Entry<T : Any>(val key: String, val data: T, val timestamp: Long)

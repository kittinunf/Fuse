package com.github.kittinunf.fuse.core

import android.content.Context
import com.github.kittinunf.fuse.core.persistence.Persistence

actual fun createTestDiskPersistence(context: Any): Persistence<ByteArray> {
    val context = context as Context
    return AndroidDiskPersistence("test-cache", context)
}

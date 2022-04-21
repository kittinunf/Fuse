package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.JvmDiskPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence

actual fun createTestDiskPersistence(context: Any): Persistence<ByteArray> = JvmDiskPersistence("test-cache")

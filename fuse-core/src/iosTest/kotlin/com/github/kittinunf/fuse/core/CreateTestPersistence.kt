package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.IosDiskPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence

actual fun createTestDiskPersistence(context: Any): Persistence<ByteArray> = IosDiskPersistence("test-cache")
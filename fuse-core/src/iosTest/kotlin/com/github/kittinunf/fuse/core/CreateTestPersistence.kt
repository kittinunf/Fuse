package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.IosPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence

actual fun createTestPersistence(): Persistence<ByteArray> = IosPersistence("test-cache")


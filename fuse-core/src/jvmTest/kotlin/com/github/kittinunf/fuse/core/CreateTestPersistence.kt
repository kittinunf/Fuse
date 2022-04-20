package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.persistence.JvmPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence

actual fun createTestPersistence(): Persistence<ByteArray> = JvmPersistence()

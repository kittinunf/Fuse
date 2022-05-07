package com.github.kittinunf.fuse.core

import android.content.Context
import com.github.kittinunf.fuse.core.persistence.JvmDiskPersistence
import kotlinx.serialization.BinaryFormat

fun AndroidDiskPersistence(name: String, context: Context, formatDriver: BinaryFormat) =
    JvmDiskPersistence(name, context.cacheDir, formatDriver)

package com.github.kittinunf.fuse.core

import android.content.Context
import com.github.kittinunf.fuse.core.persistence.JvmDiskPersistence

fun AndroidDiskPersistence(name: String, context: Context) = JvmDiskPersistence(name, context.cacheDir)

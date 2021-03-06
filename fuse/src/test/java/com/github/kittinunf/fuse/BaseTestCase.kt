package com.github.kittinunf.fuse

import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

abstract class BaseTestCase {
    val assetDir = File(System.getProperty("user.dir")).resolve("src/test/assets")
}

fun CountDownLatch.wait(seconds: Int = 15) {
    await(seconds.toLong(), TimeUnit.SECONDS)
}

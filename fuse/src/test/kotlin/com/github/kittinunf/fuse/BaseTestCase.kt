package com.github.kittinunf.fuse

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
abstract class BaseTestCase {

    val tempDirString = createTempDir().absolutePath

    val assetDir = File(System.getProperty("user.dir")).resolve("src/test/assets")

}

fun CountDownLatch.wait() {
    await(15, TimeUnit.SECONDS)
}
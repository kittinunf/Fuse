package com.github.kittinunf.fuse

import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
abstract class BaseTestCase

fun CountDownLatch.wait() {
    await(100, TimeUnit.SECONDS)
}
package com.github.kittinunf.fuse.core

import kotlin.test.BeforeTest

actual abstract class BaseTest {

    @BeforeTest
    actual fun before() {
        setUp(Unit)
    }

    actual abstract fun setUp(any: Any)
}

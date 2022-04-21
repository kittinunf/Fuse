package com.github.kittinunf.fuse.core

import org.junit.Before

actual abstract class BaseTest {

    @Before
    actual fun before() {
        setUp(Unit)
    }

    actual abstract fun setUp(any: Any)

}

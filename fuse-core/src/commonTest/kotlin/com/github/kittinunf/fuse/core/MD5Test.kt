package com.github.kittinunf.fuse.core

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MD5Test {

    @BeforeTest
    fun before() {
    }

    @Test
    fun `should return correct md5 value`() {
        val string = "Hello world!"
        assertEquals("86fb269d190d2c85f6e0468ceca42a20", string.md5())

        val anotherString = "MD5Test"
        assertEquals("add684e1863b5d900583236825a431c2", anotherString.md5())
    }
}

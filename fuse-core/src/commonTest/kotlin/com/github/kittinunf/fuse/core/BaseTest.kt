package com.github.kittinunf.fuse.core

expect abstract class BaseTest() {

    internal fun before()

    abstract fun setUp(any: Any)
}

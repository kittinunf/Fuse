package com.github.kittinunf.fuse.core

expect abstract class BaseTest() {

    fun before()

    abstract fun setUp(any: Any)
}

package com.github.kittinunf.fuse.util

inline fun <T : Pair<A, B>, A, B, R> T.let(block: (A, B) -> R): R {
    val (a, b) = this
    return block(a, b)
}

inline fun <T : Triple<A, B, C>, A, B, C, R> T.let(block: (A, B, C) -> R): R {
    val (a, b, c) = this
    return block(a, b, c)
}

 

package com.github.kittinunf.fuse.core

class Config<T>(val cacheDir: String,
                val name: String = Config.DEFAULT_NAME,
                val diskCapacity: Long = 1024 * 1024 * 20) {

    companion object {
        val DEFAULT_NAME = "Fuse-Default-Config"
    }

    var transformer: ((T) -> T) = { it }

    var convertToData: ((T) -> ByteArray)? = null

}


 

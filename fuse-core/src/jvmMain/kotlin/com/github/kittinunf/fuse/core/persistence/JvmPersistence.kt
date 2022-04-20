package com.github.kittinunf.fuse.core.persistence

import com.github.kittinunf.fuse.core.model.Entry

class JvmPersistence : Persistence<ByteArray> {

    override fun put(safeKey: String, entry: Entry<ByteArray>) {
        TODO("Not yet implemented")
    }

    override fun remove(safeKey: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll() {
        TODO("Not yet implemented")
    }

    override fun allKeys(): Set<String> {
        TODO("Not yet implemented")
    }

    override fun get(safeKey: String): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun getTimestamp(safeKey: String): Long? {
        TODO("Not yet implemented")
    }
}

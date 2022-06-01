package com.github.kittinunf.fuse.core.persistence

import com.github.kittinunf.fuse.core.formatter.JsonBinaryConverter
import com.github.kittinunf.fuse.core.model.Entry
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.io.File

class JvmDiskPersistence(name: String, private var path: File? = null) : Persistence<ByteArray> {

    private val binaryFormat: BinaryFormat = JsonBinaryConverter()

    private val cacheDirectory
        get() = path!!

    init {
        if (path != null) {
            require(path!!.isDirectory) { "Provided path must be directory" }
        } else {
            path = createTempDir(suffix = "").parentFile
        }
        // create folder inside with specific name
        path = path?.resolve("${this::class.qualifiedName}.$name/")?.also {
            if (!it.exists()) it.mkdir()
        }
    }

    override fun put(safeKey: String, entry: Entry<ByteArray>) {
        val file = createNewFileForKey(safeKey)
        val serialized = binaryFormat.encodeToByteArray(entry)
        file.writeBytes(serialized)
    }

    override fun remove(safeKey: String): Boolean {
        val destination = getFileForKey(safeKey)
        if (destination.exists().not()) return false

        return destination.delete()
    }

    override fun removeAll() {
        cacheDirectory.listFiles()?.onEach(File::delete)
    }

    override fun allKeys(): Set<String> = cacheDirectory.walk()
        .filter { it.isFile }
        .map { getEntryForKey(it.name)!!.key }.toSet()

    override fun get(safeKey: String): ByteArray? = getEntryForKey(safeKey)?.data

    override fun getTimestamp(safeKey: String): Long? = getEntryForKey(safeKey)?.timestamp

    private fun getFileForKey(safeKey: String): File = cacheDirectory.resolve(safeKey)

    private fun createNewFileForKey(safeKey: String): File = getFileForKey(safeKey).also { it.createNewFile() }

    private fun getEntryForKey(safeKey: String): Entry<ByteArray>? {
        val file = getFileForKey(safeKey)
        if (file.exists().not()) return null
        return binaryFormat.decodeFromByteArray(file.readBytes())
    }
}

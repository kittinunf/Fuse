package com.github.kittinunf.fuse.core.persistence

import com.github.kittinunf.fuse.core.formatter.JsonBinaryFormatter
import com.github.kittinunf.fuse.core.model.Entry
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSDirectoryEnumerationSkipsHiddenFiles
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByAppendingPathComponent
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.posix.memcpy

class IosDiskPersistence(name: String, private var directory: NSURL? = null, formatter: BinaryFormat = JsonBinaryFormatter()) :
    Persistence<ByteArray>, BinaryFormat by formatter {

    private val fileManager = NSFileManager()

    private val cacheDirectory
        get() = directory!!

    init {
        if (directory == null) {
            val d = NSFileManager.defaultManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask).firstOrNull() as? NSURL
            if (d == null) throw IllegalStateException("$d cannot be created")
            directory = d.URLByAppendingPathComponent("${this::class.qualifiedName}.$name")
        }

        fileManager.createDirectoryAtURL(url = cacheDirectory, withIntermediateDirectories = true, attributes = null, error = null)
    }

    override fun put(safeKey: String, entry: Entry<ByteArray>) {
        val destination = getUrlForKey(safeKey)
        val bytes = encodeToByteArray(entry)
        val data = bytes.toData()
        data.writeToURL(destination, atomically = true)
    }

    override fun remove(safeKey: String): Boolean {
        val destination = getUrlForKey(safeKey)

        if (!fileManager.fileExistsAtPath(destination.path!!)) return false

        val result = fileManager.removeItemAtURL(destination, null)
        if (!result) throw RuntimeException("Cannot delete file at path: ${destination.relativePath}")
        return result
    }

    override fun removeAll() {
        val urls = fileManager.contentsOfDirectoryAtURL(
            url = cacheDirectory,
            includingPropertiesForKeys = null,
            options = NSDirectoryEnumerationSkipsHiddenFiles,
            error = null
        )
        if (urls.isNullOrEmpty()) return

        urls.forEach { fileManager.removeItemAtURL(it as NSURL, null) }
    }

    override fun allKeys(): Set<String> {
        val urls = fileManager.contentsOfDirectoryAtURL(
            url = cacheDirectory,
            includingPropertiesForKeys = null,
            options = NSDirectoryEnumerationSkipsHiddenFiles,
            error = null
        )
        if (urls.isNullOrEmpty()) return emptySet()
        return urls.map { getEntry((it as NSURL))!!.key }.toSet()
    }

    override fun get(safeKey: String): ByteArray? = getEntry(getUrlForKey(safeKey))?.data

    override fun getTimestamp(safeKey: String): Long? = getEntry(getUrlForKey(safeKey))?.timestamp

    private fun getUrlForKey(safeKey: String): NSURL = cacheDirectory.URLByAppendingPathComponent(safeKey)
        ?: throw IllegalStateException("Cannot create NSURL destination for key: $safeKey")

    private fun getEntry(url: NSURL): Entry<ByteArray>? {
        if (!fileManager.fileExistsAtPath(url.path!!)) return null
        val data = NSData.dataWithContentsOfURL(url) ?: throw RuntimeException("Cannot retrieve data at path: ${url.relativePath}")
        val bytes = data.toByteArray()
        return decodeFromByteArray(bytes)
    }
}

internal fun ByteArray.toData(): NSData = usePinned { NSData.create(bytes = it.addressOf(0), size.convert()) }

internal fun NSData.toByteArray(): ByteArray = ByteArray(length.convert()).apply {
    usePinned { memcpy(it.addressOf(0), bytes, length.convert()) }
}

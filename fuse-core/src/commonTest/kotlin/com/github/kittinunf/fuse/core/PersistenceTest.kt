package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.model.Entry
import com.github.kittinunf.fuse.core.persistence.MemPersistence
import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

expect fun createTestDiskPersistence(context: Any): Persistence<ByteArray>

class PersistenceTest : BaseTest() {

    private lateinit var diskCache: Persistence<ByteArray>
    private lateinit var memCache: MemPersistence

    override fun setUp(any: Any) {
        diskCache = createTestDiskPersistence(any)
        memCache = MemPersistence()
    }

    @Test
    fun `should put data into persistence without error`() {
        diskCache.put("112233", Entry("112233", "hello".encodeToByteArray(), 0))
        memCache.put("112233", Entry("112233", "hello".encodeToByteArray(), 0))

        assertNotNull(diskCache)
        assertNotNull(memCache)
    }

    @Test
    fun `should be able to put data and get it back without error`() {
        diskCache.put("av1342", Entry("av1342", "hello world!".encodeToByteArray(), 0))
        memCache.put("av1342", Entry("av1342", "hello world!", 0))

        val diskResult = diskCache.get("av1342")

        assertNotNull(diskResult)
        assertEquals("hello world!", diskResult.decodeToString())

        val memResult = memCache.get("av1342")

        assertNotNull(memResult)
        assertEquals("hello world!", memResult)
    }

    @Test
    fun `should be able to delete item without error`() {
        diskCache.put("to-be-deleted", Entry("to-be-deleted", "DELETED".encodeToByteArray(), 0))
        memCache.put("to-be-deleted", Entry("to-be-deleted", "DELETED", 0))

        val diskResult = diskCache.get("to-be-deleted")
        assertNotNull(diskResult)
        assertEquals("DELETED", diskResult.decodeToString())

        assertTrue(diskCache.remove("to-be-deleted"))
        assertFalse(diskCache.remove("unknown"))
        assertFalse(diskCache.remove("to-be-deleted"))

        val memResult = memCache.get("to-be-deleted")
        assertNotNull(memResult)
        assertEquals("DELETED", memResult)

        assertTrue(memCache.remove("to-be-deleted"))
        assertFalse(memCache.remove("unknown"))
        assertFalse(memCache.remove("to-be-deleted"))
    }

    @Test
    fun `should be able to retrieve the timestamp`() {
        diskCache.put("1", Entry("1", "foo bar".encodeToByteArray(), 1650012031))
        diskCache.put("2", Entry("2", "foo bar".encodeToByteArray(), 1650012032))

        memCache.put("1", Entry("1", "foo bar", 1650012031))
        memCache.put("2", Entry("2", "foo bar", 1650012032))

        val diskTimestamp1 = diskCache.getTimestamp("1")
        val diskTimestamp2 = diskCache.getTimestamp("2")
        val diskTimestamp3 = diskCache.getTimestamp("unknown")

        assertEquals(1650012031, diskTimestamp1)
        assertEquals(1650012032, diskTimestamp2)
        assertNull(diskTimestamp3)

        val memTimestamp1 = memCache.getTimestamp("1")
        val memTimestamp2 = memCache.getTimestamp("2")
        val memTimestamp3 = memCache.getTimestamp("unknown")

        assertEquals(1650012031, memTimestamp1)
        assertEquals(1650012032, memTimestamp2)
        assertNull(memTimestamp3)
    }

    @Test
    fun `should be able to list items in cache folder`() {
        diskCache.removeAll()
        memCache.removeAll()

        diskCache.put("1", Entry("1", "foo".encodeToByteArray(), 1))
        diskCache.put("2", Entry("2", "bar".encodeToByteArray(), 2))
        diskCache.put("3", Entry("3", "foo bar".encodeToByteArray(), 3))

        memCache.put("1", Entry("1", "foo", 1))
        memCache.put("2", Entry("2", "bar", 2))
        memCache.put("3", Entry("3", "foo bar", 3))

        val diskResult = diskCache.allKeys()

        assertTrue(diskResult.isNotEmpty())
        assertContains(diskResult, "1")
        assertContains(diskResult, "2")
        assertContains(diskResult, "3")

        val memResult = diskCache.allKeys()

        assertTrue(memResult.isNotEmpty())
        assertContains(memResult, "1")
        assertContains(memResult, "2")
        assertContains(memResult, "3")
    }

    @Test
    fun `should remove all item in the cache folder`() {
        diskCache.put("1111", Entry("1234", "foo".encodeToByteArray(), 1))
        memCache.put("1111", Entry("1234", "foo".encodeToByteArray(), 1))

        var diskResult = diskCache.allKeys()
        assertContains(diskResult, "1234") // contains original key

        diskCache.removeAll()
        diskResult = diskCache.allKeys()
        assertTrue(diskResult.isEmpty())

        var memResult = memCache.allKeys()
        assertContains(memResult, "1234") // contains original key

        memCache.removeAll()
        memResult = memCache.allKeys()
        assertTrue(memResult.isEmpty())
    }
}

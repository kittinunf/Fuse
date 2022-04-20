package com.github.kittinunf.fuse.core

import com.github.kittinunf.fuse.core.model.Entry
import com.github.kittinunf.fuse.core.persistence.Persistence
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

expect fun createTestPersistence(): Persistence<ByteArray>

class PersistenceTest {

    private lateinit var testCache: Persistence<ByteArray>

    @BeforeTest
    fun beforeTest() {
        testCache = createTestPersistence()
    }

    @Test
    fun `should put data into persistence without error`() {
        testCache.put("112233", Entry("112233", "hello".encodeToByteArray(), 0))
        assertNotNull(testCache)
    }

    @Test
    fun `should be able to put data and get it back without error`() {
        testCache.put("av1342", Entry("av1342", "hello world!".encodeToByteArray(), 0))

        val result = testCache.get("av1342")

        assertNotNull(result)
        assertEquals("hello world!", result.decodeToString())
    }

    @Test
    fun `should be able to delete item without error`() {
        testCache.put("to-be-deleted", Entry("to-be-deleted", "DELETED".encodeToByteArray(), 0))

        val value = testCache.get("to-be-deleted")
        assertNotNull(value)
        assertEquals("DELETED", value.decodeToString())

        assertTrue(testCache.remove("to-be-deleted"))
        assertFalse(testCache.remove("unknown"))
        assertFalse(testCache.remove("to-be-deleted"))
    }

    @Test
    fun `should be able to retrieve the timestamp`() {
        testCache.put("1", Entry("1", "foo bar".encodeToByteArray(), 1650012031))
        testCache.put("2", Entry("2", "foo bar".encodeToByteArray(), 1650012032))

        val timestamp1 = testCache.getTimestamp("1")
        val timestamp2 = testCache.getTimestamp("2")
        val timestamp3 = testCache.getTimestamp("unknown")

        assertEquals(1650012031, timestamp1)
        assertEquals(1650012032, timestamp2)
        assertNull(timestamp3)
    }

    @Test
    fun `should be able to list items in cache folder`() {
        testCache.removeAll()

        testCache.put("1", Entry("1", "foo".encodeToByteArray(), 1))
        testCache.put("2", Entry("2", "bar".encodeToByteArray(), 2))
        testCache.put("3", Entry("3", "foo bar".encodeToByteArray(), 3))

        val result = testCache.allKeys()

        assertTrue(result.isNotEmpty())
        assertContains(result, "1")
        assertContains(result, "2")
        assertContains(result, "3")
    }

    @Test
    fun `should remove all item in the cache folder`() {
        testCache.put("1111", Entry("1234", "foo".encodeToByteArray(), 1))

        var result = testCache.allKeys()
        assertContains(result, "1234") // contains original key

        testCache.removeAll()
        result = testCache.allKeys()
        assertTrue(result.isEmpty())
    }
}

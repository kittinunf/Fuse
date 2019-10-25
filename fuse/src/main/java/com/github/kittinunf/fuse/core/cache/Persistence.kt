package com.github.kittinunf.fuse.core.cache

interface Persistence<T> {
    /**
     * Save the data supplied based on a certain mechanism which provides persistence somehow
     *
     * @param key The key associated with the value to be persisted
     * @param value The value to be persisted
     */
    fun put(key: String, value: T)

    /**
     * Remove the data associated with its particular key
     *
     * @param key The key associated with the object to be deleted from persistence
     */
    fun remove(key: String): Boolean

    /**
     * Remove all the data
     */
    fun removeAll()

    /**
     * Retrieve the keys from all values persisted
     */
    fun allKeys(): List<String>

    /**
     * Retrieve accumulated values
     */
    fun size(): Long

    /**
     * Get the record associated with its particular key
     *
     * @param key The key associated with the Record to be retrieved from persistence
     */
    fun get(key: String): T?
}

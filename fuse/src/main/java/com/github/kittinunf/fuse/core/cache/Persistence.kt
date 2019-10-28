package com.github.kittinunf.fuse.core.cache

interface Persistence<T> {
    /**
     * Save the data supplied based on a certain mechanism which provides persistence somehow
     *
     * @param safeKey The safeKey associated with the value to be persisted, this is sanitized key and it is conformed to regex <strong>[a-z0-9_-]{1,64}</strong>
     * @param key The key associated with the value to be persisted, this is an un-sanitized key which is the same as the call-site key
     * @param value The value to be persisted
     */
    fun put(safeKey: String, key: String, value: T)

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
     * Retrieve the keys from all values persisted, this is a <string>real</strong> as opposed to safeKey
     */
    fun allKeys(): Set<String>

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

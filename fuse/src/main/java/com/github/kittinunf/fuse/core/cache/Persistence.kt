package com.github.kittinunf.fuse.core.cache

internal interface Persistence<T> {
    /**
     * Save the entry supplied based on a certain mechanism which provides persistence
     *
     * @param safeKey The safeKey associated with the value to be persisted, this is sanitized key and it is conformed to regex <strong>[a-z0-9_-]{1,64}</strong>
     * @param key The key associated with the value to be persisted, this is an un-sanitized key which is the same as the call-site key
     * @param value The value to be persisted
     * @param timeToPersist The timestamp associated with the value to be persisted
     */
    fun put(safeKey: String, key: String, value: T, timeToPersist: Long)

    /**
     * Remove the entry associated with its particular key
     *
     * @param key The key associated with the object to be deleted from persistence
     * @return Boolean Whether the key was removed successfully
     */
    fun remove(key: String): Boolean

    /**
     * Remove all the entry in the persistence
     */
    fun removeAll()

    /**
     * Retrieve the keys from all values persisted, this is a <string>real</strong> as opposed to safeKey
     * @return Set<String> Set of un-sanitized keys which are readable from the call-site
     */
    fun allKeys(): Set<String>

    /**
     * Retrieve accumulated values
     * @return Long Size of the cache according to the implementation of the sub class
     */
    fun size(): Long

    /**
     * Get the value associated with its particular key
     *
     * @param key The key associated with the value to be retrieved from persistence
     */
    fun get(key: String): T?

    /**
     * Get timestamp in milliseconds associated with its particular key
     *
     * @param key The key associated with the value to be retrieved from persistence
     */
    fun getTimestamp(key: String): Long?
}

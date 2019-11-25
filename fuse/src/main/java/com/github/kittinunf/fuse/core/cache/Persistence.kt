package com.github.kittinunf.fuse.core.cache

interface Persistence<T : Any> {
    /**
     * Save the entry supplied based on a certain mechanism which provides persistence
     *
     * @param safeKey The safeKey associated with the value to be persisted, this is sanitized key and it is conformed to regex <strong>[a-z0-9_-]{1,64}</strong>
     * @param entry The Entry associated with the value to be persisted, please visit [Entry] for more information on the object's definition
     */
    fun put(safeKey: String, entry: Entry<T>)

    /**
     * Remove the entry associated with its particular safeKey
     *
     * @param safeKey The safeKey associated with the object to be deleted from persistence
     * @return Boolean Whether the safeKey was removed successfully
     */
    fun remove(safeKey: String): Boolean

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
     * Get the value associated with its particular safeKey
     *
     * @param safeKey The safeKey associated with the value to be retrieved from persistence
     */
    fun get(safeKey: String): T?

    /**
     * Get timestamp in milliseconds associated with its particular safeKey
     *
     * @param safeKey The safeKey associated with the value to be retrieved from persistence
     */
    fun getTimestamp(safeKey: String): Long?
}

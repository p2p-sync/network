package org.rmatil.sync.network.api;

import org.rmatil.sync.persistence.exceptions.InputOutputException;

/**
 * An interface for a key-value-value-key store
 *
 * @param <K> The type of the key
 * @param <V> The type of the value
 */
public interface IIdentifierManager<K, V> {

    /**
     * Add the given value to the specified key
     *
     * @param key   The key to use
     * @param value The value to use
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    void addIdentifier(K key, V value)
            throws InputOutputException;

    /**
     * Remove the key-value pair from the manager
     *
     * @param key The key to remove
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    void removeIdentifier(K key)
            throws InputOutputException;

    /**
     * Get the value of the given key.
     *
     * @param key The key from which to get the value
     *
     * @return The found value, or null, if the key was not found
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    V getValue(K key)
            throws InputOutputException;

    /**
     * Get the key of the given value
     *
     * @param value The value from which to get its key
     *
     * @return The found key, or null, if the key was not found
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    K getKey(V value)
            throws InputOutputException;
}

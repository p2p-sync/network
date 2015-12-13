package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.util.List;

/**
 * Provides access to the locations of
 * peers using their public key as peerId
 */
public interface ILocationManager {

    /**
     * Returns the storage adapter implementation of this manager
     *
     * @return The storage manager
     */
    IStorageAdapter getStorageAdapter();

    /**
     * Adds a new location to the locations of the given user
     *
     * @param user     The user to which the location is added. Must have the same public key as the peer of this manager
     * @param location The location to add
     *
     * @throws InputOutputException If adding failed
     */
    void addClientLocation(IUser user, ClientLocation location)
            throws InputOutputException;

    /**
     * Removes the given location from the list of locations
     *
     * @param user     The user from which the location should be removed. Must have the same public key as the peer of this manager
     * @param location The location to remove
     *
     * @throws InputOutputException If removing failed
     */
    void removeClientLocation(IUser user, ClientLocation location)
            throws InputOutputException;

    /**
     * Gets the list of locations of the given user
     *
     * @param user The user from which to get all client locations
     *
     * @return The list of client locations
     *
     * @throws InputOutputException If getting failed
     */
    List<ClientLocation> getClientLocations(IUser user)
            throws InputOutputException;

}

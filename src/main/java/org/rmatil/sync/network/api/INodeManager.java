package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.persistence.core.dht.secured.ISecuredDhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * Provides access to the locations of
 * peers using their public key as peerId
 */
public interface INodeManager {

    /**
     * Returns the storage adapter implementation of this manager
     *
     * @return The storage manager
     */
    ISecuredDhtStorageAdapter getStorageAdapter();

    /**
     * Adds a new location to the locations of the given user.
     *
     * @param location The location to add
     *
     * @throws InputOutputException If adding failed
     */
    void addNodeLocation(NodeLocation location)
            throws InputOutputException;

    /**
     * Removes the given location from the list of locations
     *
     * @param location The location to remove
     *
     * @throws InputOutputException If removing failed
     */
    void removeNodeLocation(NodeLocation location)
            throws InputOutputException;

    /**
     * Gets the list of locations of the given user name
     *
     * @param username The username from which to get its node locations
     *
     * @return The list of node locations
     *
     * @throws InputOutputException If getting the locations failed
     */
    List<NodeLocation> getNodeLocations(String username)
            throws InputOutputException;

    /**
     * Adds the private key of the given user to the storage
     *
     * @param user The user of which the private key is added
     *
     * @throws InputOutputException If accessing storage layer failed
     */
    void addPrivateKey(IUser user)
            throws InputOutputException;

    /**
     * Returns the private key of the user from storage
     *
     * @param user The user of which to get the private key
     *
     * @return The private key found
     *
     * @throws InputOutputException If accessing storage layer failed
     */
    PrivateKey getPrivateKey(IUser user)
            throws InputOutputException;

    /**
     * Adds the public key of the given user to the storage layer
     *
     * @param user The user of which to add its public key
     *
     * @throws InputOutputException
     */
    void addPublicKey(IUser user)
            throws InputOutputException;

    /**
     * Returns the public key of the given user
     *
     * @param user The user of which to get the public key
     *
     * @return The found public key
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    PublicKey getPublicKey(IUser user)
            throws InputOutputException;

    /**
     * Returns the public key of the given user's username
     *
     * @param username The name of the user from which to fetch its public key
     *
     * @return The found public key
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    PublicKey getPublicKey(String username)
            throws InputOutputException;

    /**
     * Add the salt from the given user
     *
     * @param user The user of which to add its salt
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    void addSalt(IUser user)
            throws InputOutputException;

    /**
     * Get the salt of the given user from the user
     *
     * @param user The user from which to get the salt
     *
     * @return The salt
     *
     * @throws InputOutputException If accessing the storage layer failed
     */
    String getSalt(IUser user)
            throws InputOutputException;

}

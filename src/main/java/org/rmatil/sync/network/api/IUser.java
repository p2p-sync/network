package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.ClientLocation;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;

/**
 * The interface for a user
 */
public interface IUser {

    /**
     * Returns the user name of the user
     *
     * @return The user name
     */
    String getUserName();

    /**
     * Returns the public key of the user
     *
     * @return The public key
     */
    PublicKey getPublicKey();

    /**
     * Returns a list of all locations of this user.
     * Note, that this list must be maintained manually
     * using a location manager
     *
     * @see ILocationManager The location manager to maintain locations
     *
     * @return The list of locations
     */
    List<ClientLocation> getClientLocations();

}

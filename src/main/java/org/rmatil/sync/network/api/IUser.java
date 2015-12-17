package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.ClientLocation;

import java.security.KeyPair;
import java.security.PrivateKey;
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
     * Returns the password of the user
     *
     * @return The password of the user
     */
    String getPassword();

    /**
     * Returns the public key of the user
     *
     * @return The public key
     */
    PublicKey getPublicKey();

    /**
     * Returns the private key of the user
     *
     * @return The private key
     */
    PrivateKey getPrivateKey();

    /**
     * Returns the public private key pair of the user
     *
     * @return The public private key pair
     */
    KeyPair getKeyPair();

    /**
     * Returns a list of all locations of this user.
     * Note, that this list must be maintained manually
     * using a location manager
     *
     * @see IClientManager The client manager to maintain locations, private and public keys
     *
     * @return The list of locations
     */
    List<ClientLocation> getClientLocations();

}

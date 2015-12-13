package org.rmatil.sync.network.core.model;

import org.rmatil.sync.network.api.IUser;

import java.security.PublicKey;
import java.util.List;

/**
 * An user implementation
 */
public class User implements IUser {

    protected String               userName;
    protected PublicKey            publicKey;
    protected List<ClientLocation> clientLocations;

    /**
     * @param userName The user name of the user
     * @param publicKey The public key of the user (used to access the DHT)
     * @param clientLocations A list of client locations
     */
    public User(String userName, PublicKey publicKey, List<ClientLocation> clientLocations) {
        this.userName = userName;
        this.publicKey = publicKey;
        this.clientLocations = clientLocations;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public List<ClientLocation> getClientLocations() {
        return clientLocations;
    }
}

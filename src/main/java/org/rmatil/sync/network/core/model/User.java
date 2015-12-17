package org.rmatil.sync.network.core.model;

import org.rmatil.sync.network.api.IUser;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * An user implementation
 */
public class User implements IUser {

    protected String               userName;
    protected String               password;
    protected PublicKey            publicKey;
    protected PrivateKey           privateKey;
    protected List<ClientLocation> clientLocations;

    /**
     * @param userName        The user name of the user
     * @param password        The password of the user
     * @param publicKey       The public key of the user
     * @param privateKey      The private key of the user
     * @param clientLocations A list of client locations
     */
    public User(String userName, String password, PublicKey publicKey, PrivateKey privateKey, List<ClientLocation> clientLocations) {
        this.userName = userName;
        this.password = password;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.clientLocations = clientLocations;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public KeyPair getKeyPair() {
        return new KeyPair(this.publicKey, this.privateKey);
    }

    @Override
    public List<ClientLocation> getClientLocations() {
        return clientLocations;
    }
}

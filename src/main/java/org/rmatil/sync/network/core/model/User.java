package org.rmatil.sync.network.core.model;

import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.encryption.Pbkdf2Factory;

import javax.crypto.SecretKey;
import java.security.*;
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
    protected SecretKey            secretKey;

    /**
     * Creates a new user object and a symmetric key for the password specified.
     * <p>
     * Note, that since a salt is used to generate the password, multiple creation
     * of the same user object will result in different secret keys.
     *
     * @param userName        The user name of the user
     * @param password        The password of the user
     * @param salt            The salt to use for generating a secret key
     * @param publicKey       The public key of the user
     * @param privateKey      The private key of the user
     * @param clientLocations A list of client locations
     *
     * @throws SecurityException If generating the symmetric key from the password failed
     */
    public User(String userName, String password, String salt, PublicKey publicKey, PrivateKey privateKey, List<ClientLocation> clientLocations)
            throws SecurityException {
        this.userName = userName;
        this.password = password;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.clientLocations = clientLocations;
        this.secretKey = Pbkdf2Factory.generateKey(this.password, salt);
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

    @Override
    public SecretKey getSecretKey() {
        return this.secretKey;
    }
}

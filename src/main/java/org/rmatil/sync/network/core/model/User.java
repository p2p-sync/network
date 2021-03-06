package org.rmatil.sync.network.core.model;

import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesKeyFactory;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * An user implementation
 */
public class User implements IUser {

    protected String             userName;
    protected String             password;
    protected String             salt;
    protected PublicKey          publicKey;
    protected PrivateKey         privateKey;
    protected List<NodeLocation> nodeLocations;
    protected SecretKey          secretKey;

    /**
     * Creates a new user object and a symmetric key for the password specified.
     * <p>
     * Note, that since a salt is used to generate the password, multiple creation
     * of the same user object will result in different secret keys.
     *
     * @param userName      The user name of the user
     * @param password      The password of the user
     * @param salt          The salt to use for generating a secret key
     * @param publicKey     The public key of the user
     * @param privateKey    The private key of the user
     * @param nodeLocations A list of node locations
     *
     * @throws SecurityException If generating the symmetric key from the password failed
     */
    public User(String userName, String password, String salt, PublicKey publicKey, PrivateKey privateKey, List<NodeLocation> nodeLocations)
            throws SecurityException {
        this.userName = userName;
        this.password = password;
        this.salt = salt;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.nodeLocations = nodeLocations;
        this.secretKey = AesKeyFactory.generateKey(this.password, salt);
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
    public String getSalt() {
        return salt;
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

    public List<NodeLocation> getNodeLocations() {
        return nodeLocations;
    }

    @Override
    public SecretKey getSecretKey() {
        return this.secretKey;
    }
}

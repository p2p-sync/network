package org.rmatil.sync.network.core;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.commons.hashing.HashingAlgorithm;
import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.security.encryption.symmetric.ISymmetricEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesEncryption;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.secured.ISecuredDhtStorageAdapter;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * An independent manager which administers node locations
 * from various users.
 * Note, that to add or remove a node location to an user, write permission is
 * ensured by using the public key of the given user.
 */
public class NodeManager implements INodeManager {

    protected final static Logger logger = LoggerFactory.getLogger(NodeManager.class);

    /**
     * A storage adapter giving access to persisted locations
     */
    protected ISecuredDhtStorageAdapter storageAdapter;

    /**
     * The content key where all locations of a particular user are stored
     */
    protected String locationContentKey;

    /**
     * The content key where the private key of a particular user is stored
     */
    protected String privateKeyContentKey;

    /**
     * The content key where the public key of a particular user is stored
     */
    protected String publicKeyContentKey;

    /**
     * The content key where the salt of a particular user is stored
     */
    protected String saltContentKey;

    /**
     * The domain key
     */
    protected String domainKey;

    protected ISymmetricEncryption aesEncryption;


    public NodeManager(ISecuredDhtStorageAdapter storageAdapter, String locationContentKey, String privateKeyContentKey, String publicKeyContentKey, String saltContentKey, String domainKey) {
        this.storageAdapter = storageAdapter;
        this.locationContentKey = locationContentKey;
        this.privateKeyContentKey = privateKeyContentKey;
        this.publicKeyContentKey = publicKeyContentKey;
        this.saltContentKey = saltContentKey;
        this.domainKey = domainKey;
        this.aesEncryption = new AesEncryption();
    }

    public void setStorageAdapter(ISecuredDhtStorageAdapter securedDhtStorageAdapter) {
        this.storageAdapter = securedDhtStorageAdapter;
    }

    @Override
    public void addNodeLocation(NodeLocation location)
            throws InputOutputException {
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                location.getUsername(),
                this.locationContentKey,
                this.domainKey
        );

        logger.trace("Adding location on location key " + dhtPathElement.getLocationKey() + ", using content key " + dhtPathElement.getContentKey() + " and domain key " + dhtPathElement.getDomainKey());

        List<NodeLocation> locations = this.getNodeLocations(location.getUsername());

        // only add the location if not yet contained
        if (! locations.contains(location)) {
            locations.add(location);

            byte[] bytes;
            try {
                bytes = ByteSerializer.toBytes(locations);
            } catch (IOException e) {
                throw new InputOutputException(e);
            }

            this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
        }
    }

    @Override
    public void removeNodeLocation(NodeLocation location)
            throws InputOutputException {
        // private key must be used to access for write
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                location.getUsername(),
                this.locationContentKey,
                this.domainKey
        );

        List<NodeLocation> locations = this.getNodeLocations(location.getUsername());
        locations.remove(location);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(locations);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public List<NodeLocation> getNodeLocations(String username)
            throws InputOutputException {
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                username,
                this.locationContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            // there was no element stored in the dht
            return new ArrayList<>();
        }

        List<NodeLocation> locations;
        try {
            locations = (List<NodeLocation>) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return locations;
    }

    @Override
    public void addPrivateKey(IUser user)
            throws InputOutputException {
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                Hash.hash(HashingAlgorithm.SHA_512, user.getUserName() + user.getSalt() + user.getPassword()),
                this.privateKeyContentKey,
                this.domainKey
        );

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(user.getPrivateKey());
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        // encrypt private key with symmetric encryption (AES)
        byte[] encrypted = this.aesEncryption.encrypt(user.getSecretKey(), bytes);

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, encrypted);
    }

    @Override
    public PrivateKey getPrivateKey(IUser user)
            throws InputOutputException {

        if (null == user.getPrivateKey()) {
            throw new InputOutputException("User private key is required to fetch its private key");
        }

        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                Hash.hash(HashingAlgorithm.SHA_512, user.getUserName() + user.getSalt() + user.getPassword()),
                this.privateKeyContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            // there was no element stored in the dht
            return null;
        }

        // decrypt private key
        byte[] decrypted = this.aesEncryption.decrypt(user.getSecretKey(), bytes);

        PrivateKey privateKey;
        try {
            privateKey = (PrivateKey) ByteSerializer.fromBytes(decrypted);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return privateKey;
    }

    public void addPublicKey(IUser user)
            throws InputOutputException {

        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                user.getUserName(),
                this.publicKeyContentKey,
                this.domainKey
        );

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(user.getPublicKey());
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public PublicKey getPublicKey(IUser user)
            throws InputOutputException {
        return this.getPublicKey(user.getUserName());
    }

    @Override
    public PublicKey getPublicKey(String username)
            throws InputOutputException {
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                username,
                this.publicKeyContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            // there was no element stored in the dht
            return null;
        }

        PublicKey publicKey;
        try {
            publicKey = (PublicKey) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return publicKey;
    }

    @Override
    public void addSalt(IUser user)
            throws InputOutputException {
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                user.getUserName(),
                this.saltContentKey,
                this.domainKey
        );

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(user.getSalt());
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public String getSalt(IUser user)
            throws InputOutputException {
        SecuredDhtPathElement dhtPathElement = new SecuredDhtPathElement(
                user.getUserName(),
                this.saltContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            // there was no element stored in the dht
            return null;
        }

        String salt;
        try {
            salt = (String) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return salt;
    }

    @Override
    public ISecuredDhtStorageAdapter getStorageAdapter() {
        return this.storageAdapter;
    }

}

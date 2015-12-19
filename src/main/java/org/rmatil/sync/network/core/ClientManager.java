package org.rmatil.sync.network.core;

import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.security.encryption.symmetric.ISymmetricEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesEncryption;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * An independent manager which administers client locations
 * from various users.
 * Note, that to add or remove a client location to an user, write permission is
 * ensured by using the public key of the given user.
 */
public class ClientManager implements IClientManager {

    protected final static Logger logger = LoggerFactory.getLogger(ClientManager.class);

    /**
     * A storage adapter giving access to persisted locations
     */
    protected IStorageAdapter storageAdapter;

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
     * The domain key
     */
    protected String domainKey;

    protected ISymmetricEncryption aesEncryption;


    public ClientManager(IStorageAdapter storageAdapter, String locationContentKey, String privateKeyContentKey, String publicKeyContentKey, String domainKey) {
        this.storageAdapter = storageAdapter;
        this.locationContentKey = locationContentKey;
        this.privateKeyContentKey = privateKeyContentKey;
        this.publicKeyContentKey = publicKeyContentKey;
        this.domainKey = domainKey;
        this.aesEncryption = new AesEncryption();
    }

    @Override
    public void addClientLocation(IUser user, ClientLocation location)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
                this.locationContentKey,
                this.domainKey
        );

        logger.trace("Adding location on location key " + dhtPathElement.getLocationKey() + ", using content key " + dhtPathElement.getContentKey() + " and domain key " + dhtPathElement.getDomainKey());

        List<ClientLocation> locations = this.getClientLocations(user);
        locations.add(location);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(locations);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);

        user.getClientLocations().clear();
        user.getClientLocations().addAll(locations);
    }

    @Override
    public void removeClientLocation(IUser user, ClientLocation location)
            throws InputOutputException {
        // private key must be used to access for write
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
                this.locationContentKey,
                this.domainKey
        );

        List<ClientLocation> locations = this.getClientLocations(user);
        locations.remove(location);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(locations);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);

        user.getClientLocations().clear();
        user.getClientLocations().addAll(locations);
    }

    @Override
    public List<ClientLocation> getClientLocations(IUser user)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
                this.locationContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            // there was no element stored in the dht
            return new ArrayList<>();
        }

        List<ClientLocation> locations;
        try {
            locations = (List<ClientLocation>) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        user.getClientLocations().clear();
        user.getClientLocations().addAll(locations);

        return locations;
    }

    @Override
    public void addPrivateKey(IUser user)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName() + user.getPassword(),
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

        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName() + user.getPassword(),
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

        DhtPathElement dhtPathElement = new DhtPathElement(
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

    public PublicKey getPublicKey(IUser user)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
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
    public IStorageAdapter getStorageAdapter() {
        return this.storageAdapter;
    }

}

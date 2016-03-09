package org.rmatil.sync.network.core;

import org.rmatil.sync.commons.hashing.Hash;
import org.rmatil.sync.commons.hashing.HashingAlgorithm;
import org.rmatil.sync.network.api.IIdentifierManager;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.secured.ISecuredDhtStorageAdapter;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * A manager to dispense identifier-value pairs.
 */
public class IdentifierManager implements IIdentifierManager<String, UUID> {

    /**
     * The username which is used as location key
     */
    protected String username;

    /**
     * The content key of the identifiers
     */
    protected String identifierContentKey;

    /**
     * The domain key of the identifiers
     */
    protected String domainKey;

    /**
     * The storage adapter to write
     */
    protected ISecuredDhtStorageAdapter storageAdapter;

    public IdentifierManager(ISecuredDhtStorageAdapter storageAdapter, String username, String identifierContentKey, String domainKey) {
        this.storageAdapter = storageAdapter;
        this.username = username;
        this.identifierContentKey = identifierContentKey;
        this.domainKey = domainKey;
    }

    @Override
    public synchronized void addIdentifier(String key, UUID value)
            throws InputOutputException {
        SecuredDhtPathElement keyDhtPathElement = new SecuredDhtPathElement(
                this.username,
                Hash.hash(HashingAlgorithm.SHA_256, this.identifierContentKey + key),
                this.domainKey
        );

        SecuredDhtPathElement valueDhtPathElement = new SecuredDhtPathElement(
                this.username,
                Hash.hash(HashingAlgorithm.SHA_256, this.identifierContentKey + value),
                this.domainKey
        );

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value.toString().getBytes(StandardCharsets.UTF_8);

        // store key and value
        this.storageAdapter.persist(StorageType.FILE, keyDhtPathElement, valueBytes);
        this.storageAdapter.persist(StorageType.FILE, valueDhtPathElement, keyBytes);
    }

    @Override
    public synchronized void removeIdentifier(String key)
            throws InputOutputException {
        SecuredDhtPathElement keyDhtPathElement = new SecuredDhtPathElement(
                this.username,
                Hash.hash(HashingAlgorithm.SHA_256, this.identifierContentKey + key),
                this.domainKey
        );

        // try to get associated value
        UUID value = this.getValue(key);
        if (null != value) {
            // there is a value associated -> remove it too
            SecuredDhtPathElement valueDhtPathElement = new SecuredDhtPathElement(
                    this.username,
                    Hash.hash(HashingAlgorithm.SHA_256, this.identifierContentKey + value),
                    this.domainKey
            );

            this.storageAdapter.delete(valueDhtPathElement);
        }

        this.storageAdapter.delete(keyDhtPathElement);
    }

    @Override
    public void moveKey(String oldKey, String newKey)
            throws InputOutputException {
        UUID value = this.getValue(oldKey);

        if (null == value) {
            throw new InputOutputException("Could not move value from " + oldKey + " to " + newKey + ": No value attached to the old key");
        }

        // move data
        this.removeIdentifier(oldKey);
        this.addIdentifier(newKey, value);
    }

    @Override
    public synchronized UUID getValue(String key)
            throws InputOutputException {
        SecuredDhtPathElement keyDhtPathElement = new SecuredDhtPathElement(
                this.username,
                Hash.hash(HashingAlgorithm.SHA_256, this.identifierContentKey + key),
                this.domainKey
        );

        byte[] uuidStringBytes = this.storageAdapter.read(keyDhtPathElement);

        if (0 == uuidStringBytes.length) {
            return null;
        }

        return UUID.fromString(new String(uuidStringBytes, StandardCharsets.UTF_8));
    }

    @Override
    public synchronized String getKey(UUID value)
            throws InputOutputException {
        SecuredDhtPathElement valueDhtPathElement = new SecuredDhtPathElement(
                this.username,
                Hash.hash(HashingAlgorithm.SHA_256, this.identifierContentKey + value),
                this.domainKey
        );

        byte[] keyStringBytes = this.storageAdapter.read(valueDhtPathElement);

        if (0 == keyStringBytes.length) {
            // value not found
            return null;
        }

        return new String(keyStringBytes, StandardCharsets.UTF_8);
    }
}

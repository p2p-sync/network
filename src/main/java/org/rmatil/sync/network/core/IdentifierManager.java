package org.rmatil.sync.network.core;

import org.rmatil.sync.network.api.IIdentifierManager;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
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
    protected IStorageAdapter storageAdapter;

    public IdentifierManager(IStorageAdapter storageAdapter, String username, String identifierContentKey, String domainKey) {
        this.storageAdapter = storageAdapter;
        this.username = username;
        this.identifierContentKey = identifierContentKey;
        this.domainKey = domainKey;
    }

    @Override
    public synchronized void addIdentifier(String key, UUID value)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        IdentifierMap<String, UUID> identifierMap = this.getIdentifierMap();

        identifierMap.getKeyMap().put(key, value);
        identifierMap.getValueMap().put(value, key);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(identifierMap);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public synchronized void removeIdentifier(String key)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        IdentifierMap<String, UUID> identifierMap = this.getIdentifierMap();

        UUID value = identifierMap.getKeyMap().get(key);
        identifierMap.getKeyMap().remove(key);
        identifierMap.getValueMap().remove(value);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(identifierMap);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public synchronized UUID getValue(String key)
            throws InputOutputException {
        IdentifierMap<String, UUID> identifierMap = this.getIdentifierMap();

        return identifierMap.getKeyMap().get(key);
    }

    @Override
    public synchronized String getKey(UUID value)
            throws InputOutputException {
        IdentifierMap<String, UUID> identifierMap = this.getIdentifierMap();

        return identifierMap.getValueMap().get(value);
    }

    @Override
    public synchronized IdentifierMap<String, UUID> getIdentifierMap()
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            return new IdentifierMap<>();
        }

        IdentifierMap<String, UUID> identifierMap;
        try {
            identifierMap = (IdentifierMap<String, UUID>) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return identifierMap;
    }
}

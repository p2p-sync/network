package org.rmatil.sync.network.core;

import org.rmatil.sync.network.api.IIdentifierManager;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A manager to dispense identifier-value pairs.
 */
public class IdentifierManager implements IIdentifierManager<String> {

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
    public void addIdentifier(UUID key, String value)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        Map<UUID, String> identifierMap = this.getIdentifierMap();

        if (null == identifierMap) {
            identifierMap = new HashMap<>();
        }

        identifierMap.put(key, value);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(identifierMap);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public void removeIdentifier(UUID key)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        Map<UUID, String> identifierMap = this.getIdentifierMap();

        if (null == identifierMap) {
            return;
        }

        identifierMap.remove(key);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(identifierMap);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public String getIdentifierValue(UUID key)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            return null;
        }

        Map<UUID, String> identifierMap;
        try {
            identifierMap = (Map<UUID, String>) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return identifierMap.get(key);
    }

    @Override
    public Map<UUID, String> getIdentifierMap()
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                this.username,
                this.identifierContentKey,
                this.domainKey
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            return null;
        }

        Map<UUID, String> identifierMap;
        try {
            identifierMap = (Map<UUID, String>) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e) {
            throw new InputOutputException(e);
        }

        return identifierMap;
    }
}

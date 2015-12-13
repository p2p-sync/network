package org.rmatil.sync.network.core;

import org.rmatil.sync.network.api.ILocationManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.api.StorageType;
import org.rmatil.sync.persistence.core.dht.DhtPathElement;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An independent manager which administers client locations
 * from various users.
 * Note, that to add or remove a client location to an user, write permission is
 * ensured by using the public key of the given user.
 */
public class LocationManager implements ILocationManager {

    /**
     * A storage adapter giving access to persisted locations
     */
    protected IStorageAdapter storageAdapter;

    public LocationManager(IStorageAdapter storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    @Override
    public void addClientLocation(IUser user, ClientLocation location)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
                Config.DEFAULT.getLocationsContentKey(),
                user.getPublicKey()
        );

        List<ClientLocation> locations = this.getClientLocations(user);
        locations.add(location);

        byte[] bytes;
        try {
            bytes = ByteSerializer.toBytes(locations);
        } catch (IOException e) {
            throw new InputOutputException(e);
        }

        this.storageAdapter.persist(StorageType.FILE, dhtPathElement, bytes);
    }

    @Override
    public void removeClientLocation(IUser user, ClientLocation location)
            throws InputOutputException {
        // private key must be used to access for write
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
                Config.DEFAULT.getLocationsContentKey(),
                user.getPublicKey()
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
    }

    @Override
    public List<ClientLocation> getClientLocations(IUser user)
            throws InputOutputException {
        DhtPathElement dhtPathElement = new DhtPathElement(
                user.getUserName(),
                Config.DEFAULT.getLocationsContentKey(),
                user.getPublicKey()
        );

        byte[] bytes = this.storageAdapter.read(dhtPathElement);

        if (0 == bytes.length) {
            // there was no element stored in the dht
            return new ArrayList<>();
        }

        List<ClientLocation> locations;
        try {
            locations = (List<ClientLocation>) ByteSerializer.fromBytes(bytes);
        } catch (IOException | ClassNotFoundException e){
            throw new InputOutputException(e);
        }

        return locations;
    }

    @Override
    public IStorageAdapter getStorageAdapter() {
        return this.storageAdapter;
    }

}

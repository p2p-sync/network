package org.rmatil.sync.network.api;

import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.util.Map;
import java.util.UUID;

public interface IIdentifierManager<T> {

    void addIdentifier(UUID key, T value)
            throws InputOutputException;

    void removeIdentifier(UUID key)
            throws InputOutputException;

    T getIdentifierValue(UUID key)
            throws InputOutputException;

    Map<UUID, T> getIdentifierMap()
            throws InputOutputException;
}

package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.exception.ConnectionFailedException;

/**
 * An interface for response callbacks.
 */
public interface IResponseCallback {

    /**
     * Should be called from the object data reply handler, once
     * he receives a response to a previous request of the network handler
     * implementing this interface.
     *
     * @see org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler
     * @see org.rmatil.sync.network.core.ANetworkHandler
     *
     * @throws ConnectionFailedException If an error occurred during result computation
     */
    void onResponse(IResponse response)
            throws ConnectionFailedException;
}

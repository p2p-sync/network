package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;

import java.io.Serializable;
import java.util.UUID;

/**
 * The common interface for responses
 * which are handle by an implementation of INetworkHandler.
 *
 * @see INetworkHandler
 */
public interface IResponse extends Serializable {

    /**
     * The id of the information exchange
     *
     * @return The id of the exchange
     */
    UUID getExchangeId();

    /**
     * The node device which sends this request
     *
     * @return The node device which has sent this request
     */
    ClientDevice getClientDevice();

    /**
     * Returns the receiver address to which this response should be sent
     *
     * @return The receiver address
     */
    NodeLocation getReceiverAddress();
}

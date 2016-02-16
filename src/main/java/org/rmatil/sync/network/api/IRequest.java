package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * The common interface for requests which are handled by the ObjectDataReplyHandler.
 *
 * @see org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler
 */
public interface IRequest extends Serializable {

    /**
     * Returns the receiver address of this request
     *
     * @return All addresses to which this request should be sent
     */
    List<NodeLocation> getReceiverAddresses();

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
}

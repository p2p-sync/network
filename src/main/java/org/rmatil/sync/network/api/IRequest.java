package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * The common interface for requests
 * which are handled by the ObjectDataReply.
 * <p>
 * Each request has to implement the logic to create a response (if necessary)
 * in {@link Runnable#run()}.
 *
 * @see INetworkHandler
 */
public interface IRequest extends Serializable, Runnable {

    /**
     * Returns the receiver address of this request
     *
     * @return All addresses to which this request should be sent
     */
    List<ClientLocation> getReceiverAddresses();

    /**
     * Sets the client to send back the response of this request
     *
     * @param client The client used to send the response to this request
     */
    void setClient(IClient client);

    /**
     * The id of the information exchange
     *
     * @return The id of the exchange
     */
    UUID getExchangeId();

    /**
     * The client device which sends this request
     *
     * @return The client device which has sent this request
     */
    ClientDevice getClientDevice();

    /**
     * Send a response back to the client which sends this request
     *
     * @param response The response to send back
     */
    void sendResponse(IResponse response);
}

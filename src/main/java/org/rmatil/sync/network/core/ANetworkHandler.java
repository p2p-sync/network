package org.rmatil.sync.network.core;

import net.tomp2p.futures.FutureDirect;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles communication between multiple clients.
 * Requires, that each client to which a request has been sent must respond with
 * the corresponding response to the configured request.
 * <p>
 * Once all clients responded, handleResult() will be invoked.
 * Note, that the concrete result computation is implemented by the extending class.
 *
 * @param <T> The result type which should be returned, once the information exchange between clients has been completed
 */
public abstract class ANetworkHandler<T> implements INetworkHandler<T> {

    private final static Logger logger = LoggerFactory.getLogger(ANetworkHandler.class);

    protected final static int  MAX_WAITING_RETRIES = 5;
    protected final static long MAX_WAITING_TIME    = 1000L;

    /**
     * The user of this client
     */
    protected IUser user;

    /**
     * The client manager to access meta information
     */
    protected ClientManager clientManager;

    /**
     * The client of this device
     */
    protected IClient client;

    /**
     * The initial request which has been sent to all clients
     */
    protected IRequest request;

    /**
     * A map having the client device which responded along with its response
     * representing all clients which have responded to the initial request
     */
    protected Map<ClientDevice, FutureDirect> notifiedClients;

    protected Map<ClientDevice, IResponse> respondedClients;

    /**
     * @param user          The user of this client
     * @param clientManager The client manager to access meta information
     * @param client        The client of this device
     * @param request       The initial request which will be sent to all clients
     */
    public ANetworkHandler(IUser user, ClientManager clientManager, IClient client, IRequest request) {
        this.user = user;
        this.clientManager = clientManager;
        this.client = client;
        this.request = request;

        this.notifiedClients = new HashMap<>();
        this.respondedClients = new HashMap<>();
    }

    @Override
    public T call()
            throws ConnectionFailedException {
        logger.info("Sending request " + this.request.getExchangeId() + " to clients");
        try {

            List<ClientLocation> clientLocations;
            try {
                clientLocations = this.clientManager.getClientLocations(this.user);
            } catch (InputOutputException e) {
                throw new ConnectionFailedException("Could not fetch client locations to send file offer to. Message: " + e.getMessage(), e);
            }
            logger.trace("Found " + clientLocations.size() + " other clients");

            // offer file
            for (ClientLocation entry : clientLocations) {
                logger.debug("Sending request " + this.request.getExchangeId() + " to client " + entry.getIpAddress() + ":" + entry.getPort());
                try {
                    FutureDirect futureDirect = this.client.sendDirect(entry.getPeerAddress(), this.request);
                    ClientDevice clientDevice = new ClientDevice(
                            this.client.getUser().getUserName(),
                            entry.getClientDeviceId(),
                            entry.getPeerAddress()
                    );

                    this.notifiedClients.put(clientDevice, futureDirect);
                } catch (ObjectSendFailedException e) {
                    logger.error("Failed to send request to client " + entry.getClientDeviceId() + " (" + entry.getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getPeerAddress().tcpPort() + "). Message: " + e.getMessage());
                }
            }

            this.waitForNotifiedClients();

            return this.handleResult();

        } catch (Exception e) {
            logger.error("Error in ANetworkHandler thread. Message: " + e.getMessage(), e);
        }

        return null;
    }

    @Override
    public boolean waitForNotifiedClients() {
        int retries = 0;
        while (retries < MAX_WAITING_RETRIES && this.respondedClients.size() < this.notifiedClients.size()) {
            for (Map.Entry<ClientDevice, FutureDirect> entry : this.notifiedClients.entrySet()) {
                FutureDirect futureDirect = entry.getValue();
                try {
                    futureDirect.await(MAX_WAITING_TIME);
                } catch (InterruptedException e) {
                    logger.error("Failed to wait for future direct of client " + entry.getKey().getClientDeviceId() + " (" + entry.getKey().getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getKey().getPeerAddress().tcpPort() + "). Thread got interrupted while waiting for futureDirect to complete. Message: " + e.getMessage());
                }

                if (futureDirect.isCompleted()) {
                    if (futureDirect.isSuccess()) {
                        try {
                            this.respondedClients.put(entry.getKey(), (IResponse) futureDirect.object());
                        } catch (ClassNotFoundException | IOException e) {
                            logger.error("Failed to get response object from futureDirect of client " + entry.getKey().getClientDeviceId() + " (" + entry.getKey().getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getKey().getPeerAddress().tcpPort() + "). Message: " + e.getMessage(), e);
                        }
                    } else {
                        logger.error("Client " + entry.getKey().getClientDeviceId() + " (" + entry.getKey().getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getKey().getPeerAddress().tcpPort() + ") did not respond successfully. Message: " + futureDirect.failedReason());
                    }
                } else {
                    logger.debug("FutureDirect of client " + entry.getKey().getClientDeviceId() + " (" + entry.getKey().getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getKey().getPeerAddress().tcpPort() + ") is not completed yet after waiting " + MAX_WAITING_TIME + " milliseconds. This was retry " + retries + " of max. retries " + MAX_WAITING_RETRIES);
                }
            }

            retries++;
        }

        // if all clients responded successfully, their response is stored in respondedClients
        return this.notifiedClients.size() == this.respondedClients.size();

    }

    /**
     * Should be invoked once all protocol steps are completed.
     * Is called from call once all clients to which a request has been sent responded.
     *
     * @return The result after all protocol steps are performed. May be null
     *
     * @throws ConnectionFailedException If an error occurred during result computation
     */
    protected abstract T handleResult()
            throws ConnectionFailedException;

}

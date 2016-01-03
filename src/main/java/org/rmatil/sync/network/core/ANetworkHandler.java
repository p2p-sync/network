package org.rmatil.sync.network.core;

import net.tomp2p.futures.FutureDirect;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
public abstract class ANetworkHandler<T> implements INetworkHandler<T>, IResponseCallback {

    private final static Logger logger = LoggerFactory.getLogger(ANetworkHandler.class);

    public final long MAX_WAITING_TIME = 30000L;

    /**
     * The countdown latch which will be completed once all
     * notified clients have responded
     */
    protected CountDownLatch countDownLatch;

    /**
     * The client of this device
     */
    protected IClient client;

    /**
     * A map having the client device which responded along with its response
     * representing all clients which have responded to the initial request
     */
    protected Map<ClientDevice, FutureDirect> notifiedClients;

    /**
     * Indicates whether a request has been sent yet to any client
     */
    protected boolean hasStartedToNotify = false;

    /**
     * @param client The client of this device
     */
    public ANetworkHandler(IClient client) {
        this.client = client;
        this.notifiedClients = new HashMap<>();
    }

    @Override
    public abstract void run();

    @Override
    public void sendRequest(IRequest request)
            throws ConnectionFailedException {
        this.hasStartedToNotify = true;

        List<ClientLocation> clientLocations = request.getReceiverAddresses();

        // offer file
        for (ClientLocation entry : clientLocations) {
            if (entry.getPeerAddress().equals(this.client.getPeerAddress())) {
                logger.debug("Ignoring receiver address " + entry.getIpAddress() + ":" + entry.getPort() + " since it is the own client's address");
                continue;
            }

            logger.debug("Sending request " + request.getExchangeId() + " to client " + entry.getIpAddress() + ":" + entry.getPort());
            try {
                this.client.getObjectDataReplyHandler().addCallbackHandler(request.getExchangeId(), this);
                FutureDirect futureDirect = this.client.sendDirect(entry.getPeerAddress(), request);

                futureDirect.await();

                if (futureDirect.isFailed()) {
                    throw new ObjectSendFailedException("Failed to sent request " + request.getExchangeId() + ". Message: " + futureDirect.failedReason());
                }

                ClientDevice clientDevice = new ClientDevice(
                        this.client.getUser().getUserName(),
                        entry.getClientDeviceId(),
                        entry.getPeerAddress()
                );

                this.notifiedClients.put(clientDevice, futureDirect);
            } catch (ObjectSendFailedException | InterruptedException e) {
                logger.error("Failed to send request to client " + entry.getClientDeviceId() + " (" + entry.getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getPeerAddress().tcpPort() + "). Message: " + e.getMessage());
            }
        }

        // init count down latch with size of all clients
        this.countDownLatch = new CountDownLatch(this.notifiedClients.size());
    }

    @Override
    public void await()
            throws InterruptedException {
        this.countDownLatch.await(MAX_WAITING_TIME, TimeUnit.MILLISECONDS);
    }

    @Override
    public void await(long timeout, TimeUnit timeUnit)
            throws InterruptedException {
        this.countDownLatch.await(timeout, timeUnit);
    }

    @Override
    public boolean isCompleted() {
        return null != this.countDownLatch && 0 == this.countDownLatch.getCount();
    }

    @Override
    public int getProgress() {
        if (this.hasStartedToNotify && this.notifiedClients.size() > 0) {
            return Math.round(((this.notifiedClients.size() - this.countDownLatch.getCount()) / this.notifiedClients.size()) * 100);
        }

        if (this.hasStartedToNotify) {
            return 100;
        }

        return 0;
    }

    /**
     * <p color="red">Note, that each time a response is received, the count down latch
     * should be decreased by one. Otherwise, {@link ANetworkHandler#await()} and/or {@link ANetworkHandler#await(long, TimeUnit)}
     * will wait forever.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public abstract void onResponse(IResponse response);


    @Override
    public abstract T getResult();
}

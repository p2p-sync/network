package org.rmatil.sync.network.core;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    public static final long MAX_WAITING_TIME = 30000L;

    protected CountDownLatch waitForSentCountDownLatch;

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
    final protected Map<ClientDevice, FutureDirect> notifiedClients;

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
        this.waitForSentCountDownLatch = new CountDownLatch(1);
    }

    @Override
    public abstract void run();

    @Override
    public void sendRequest(IRequest request)
            throws ConnectionFailedException {
        this.hasStartedToNotify = true;

        List<ClientLocation> clientLocations = request.getReceiverAddresses();

        // offer file
        final CountDownLatch latch = new CountDownLatch(clientLocations.size());
        for (ClientLocation entry : clientLocations) {
            if (entry.getPeerAddress().equals(this.client.getPeerAddress())) {
                logger.debug("Ignoring receiver address " + entry.getIpAddress() + ":" + entry.getPort() + " since it is the own client's address");
                latch.countDown();
                continue;
            }

            logger.debug("Sending request " + request.getExchangeId() + " to client " + entry.getIpAddress() + ":" + entry.getPort() + ". Timestamp: " + System.currentTimeMillis());
            try {
                final FutureDirect futureDirect = this.client.sendDirect(entry.getPeerAddress(), request);

                final ClientDevice clientDevice = new ClientDevice(
                        client.getUser().getUserName(),
                        entry.getClientDeviceId(),
                        entry.getPeerAddress()
                );


                // if the future is already completed, the future listener will not be called,
                // therefore we have to countdown already here!
                if (futureDirect.isCompleted()) {
                    if (futureDirect.isFailed()) {
                        logger.error("Failed to sent request " + request.getExchangeId() + ". Message: " + futureDirect.failedReason());
                    }

                    notifiedClients.put(clientDevice, futureDirect);
                    latch.countDown();
                } else {
                    // operation complete is only invoked, if the future is not completed yet!
                    futureDirect.addListener(new BaseFutureAdapter<FutureDirect>() {
                        @Override
                        public void operationComplete(FutureDirect future)
                                throws Exception {
                            logger.trace("Got operationComplete");
                            // we got a response here
                            latch.countDown();

                            if (futureDirect.isFailed()) {
                                logger.error("Failed to sent request " + request.getExchangeId() + ". Message: " + futureDirect.failedReason());
                            }

                            notifiedClients.put(clientDevice, futureDirect);
                        }
                    });
                }

            } catch (ObjectSendFailedException e) {
                logger.error("Failed to send request to client " + entry.getClientDeviceId() + " (" + entry.getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getPeerAddress().tcpPort() + "). Message: " + e.getMessage());
            }
        }

        // await using a listener instead of the future direct directly
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Got interrupted while waiting for completion of sending request to all clients. Message: " + e.getMessage(), e);
        }

        // init count down latch with size of all clients
        this.countDownLatch = new CountDownLatch(this.notifiedClients.size());
        this.waitForSentCountDownLatch.countDown();
    }

    @Override
    public void await()
            throws InterruptedException {
        // first wait that count down latch for sending is initialized
        this.waitForSentCountDownLatch.await(MAX_WAITING_TIME, TimeUnit.MILLISECONDS);
        this.countDownLatch.await(MAX_WAITING_TIME, TimeUnit.MILLISECONDS);

    }

    @Override
    public void await(long timeout, TimeUnit timeUnit)
            throws InterruptedException {
        // first wait that count down latch for sending is initialized
        this.waitForSentCountDownLatch.await(timeout, timeUnit);
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
     * <p>
     * {@inheritDoc}
     */
    @Override
    public abstract void onResponse(IResponse response);


    @Override
    public abstract T getResult();
}

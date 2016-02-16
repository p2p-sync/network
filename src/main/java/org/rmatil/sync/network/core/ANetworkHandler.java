package org.rmatil.sync.network.core;

import net.tomp2p.futures.FutureDirect;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.messaging.FutureDirectListener;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Handles communication between multiple clients.
 * Requires, that each node to which a request has been sent must respond with
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

    /**
     * The countdown latch which will be completed once all
     * connected clients have been notified
     */
    private final CountDownLatch waitForSentCountDownLatch;

    /**
     * The countdown latch which will be completed once all
     * notified clients have responded
     */
    private CountDownLatch countDownLatch;

    /**
     * The node of this device
     */
    protected INode node;

    /**
     * A map having the node device which responded along with its response
     * representing all clients which have responded to the initial request
     */
    protected final Map<ClientDevice, FutureDirect> notifiedClients;

    /**
     * Indicates whether a request has been sent yet to any node
     */
    protected boolean hasStartedToNotify = false;

    /**
     * @param node The node of this device
     */
    public ANetworkHandler(INode node) {
        this.node = node;
        this.notifiedClients = new HashMap<>();
        this.waitForSentCountDownLatch = new CountDownLatch(1);
    }

    @Override
    public abstract void run();

    @Override
    public void sendRequest(IRequest request)
            throws ConnectionFailedException {
        this.hasStartedToNotify = true;

        List<NodeLocation> nodeLocations = request.getReceiverAddresses();

        // offer file
        for (NodeLocation entry : nodeLocations) {
            if (entry.getPeerAddress().equals(this.node.getPeerAddress())) {
                logger.debug("Ignoring receiver address " + entry.getIpAddress() + ":" + entry.getPort() + " since it is the own node's address");
                continue;
            }

            logger.debug("Sending request " + request.getExchangeId() + " to node " + entry.getIpAddress() + ":" + entry.getPort() + ". Timestamp: " + System.currentTimeMillis());
            try {
                FutureDirect futureDirect = this.node.sendDirect(entry.getPeerAddress(), request);
                FutureDirectListener futureDirectListener = new FutureDirectListener();
                futureDirect.addListener(futureDirectListener);

                // await using a listener instead of the future direct directly
                futureDirectListener.await();

                if (futureDirect.isFailed()) {
                    throw new ObjectSendFailedException("Failed to sent request " + request.getExchangeId() + ". Message: " + futureDirect.failedReason());
                }

                ClientDevice clientDevice = new ClientDevice(
                        this.node.getUser().getUserName(),
                        entry.getClientDeviceId(),
                        entry.getPeerAddress()
                );

                this.notifiedClients.put(clientDevice, futureDirect);

            } catch (ObjectSendFailedException e) {
                logger.error("Failed to send request to node " + entry.getClientDeviceId() + " (" + entry.getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getPeerAddress().tcpPort() + "). Removing this node from connected node locations. Message: " + e.getMessage());
                try {
                    this.node.getNodeManager().removeNodeLocation(this.node.getUser(), entry);
                } catch (InputOutputException e1) {
                    logger.error("Failed to remove node location " + entry);
                }
            } catch (InterruptedException e) {
                logger.error("Failed to send request to node " + entry.getClientDeviceId() + " (" + entry.getPeerAddress().inetAddress().getHostAddress() + ":" + entry.getPeerAddress().tcpPort() + "). Got interrupted while waiting for completion. Message: " + e.getMessage());
            }
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

    @Override
    public void onResponse(IResponse response) {
        logger.info("Received response for exchange " + response.getExchangeId() + " of node " + response.getClientDevice().getClientDeviceId() + " (" + response.getClientDevice().getPeerAddress().inetAddress().getHostName() + ":" + response.getClientDevice().getPeerAddress().tcpPort() + ")");

        try {
            this.waitForSentCountDownLatch.await(MAX_WAITING_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Got interrupted while waiting that request has been sent to al clients. Message: " + e.getMessage());
        }

        this.countDownLatch.countDown();
    }


    @Override
    public abstract T getResult();
}

package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.INode;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IRequestCallback;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyRequestHandler implements IRequestCallback {

    private static final Logger logger = LoggerFactory.getLogger(DummyRequestHandler.class);

    protected INode    client;
    protected IRequest request;

    @Override
    public void setNode(INode client) {
        this.client = client;
    }

    @Override
    public void setRequest(IRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        IResponse response = new DummyResponse(
                this.request.getExchangeId(),
                new ClientDevice(
                        this.client.getUser().getUserName(),
                        this.client.getClientDeviceId(),
                        this.client.getPeerAddress()
                ),
                new NodeLocation(
                        this.client.getUser().getUserName(),
                        this.request.getClientDevice().getClientDeviceId(),
                        this.request.getClientDevice().getPeerAddress()
                )
        );

        logger.info("Sending back a dummy response for request " + this.request.getExchangeId());

        this.client.sendDirect(
                response.getReceiverAddress(),
                response
        );
    }
}

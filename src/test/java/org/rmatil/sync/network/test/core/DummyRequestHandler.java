package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IRequestCallback;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyRequestHandler implements IRequestCallback {

    private static final Logger logger = LoggerFactory.getLogger(DummyRequestHandler.class);

    protected IClient client;
    protected IRequest request;

    @Override
    public void setClient(IClient client) {
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
                new ClientLocation(
                        this.request.getClientDevice().getClientDeviceId(),
                        this.request.getClientDevice().getPeerAddress()
                )
        );

        logger.info("Sending back a dummy response for request " + this.request.getExchangeId());

        this.client.sendDirect(
                this.request.getClientDevice().getPeerAddress(),
                response
        );
    }
}

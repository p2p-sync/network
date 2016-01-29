package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.ANetworkHandler;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DummyNetworkHandler extends ANetworkHandler<Boolean> {

    private final static Logger logger = LoggerFactory.getLogger(DummyNetworkHandler.class);

    protected IClientManager clientManager;

    public DummyNetworkHandler(IClient client, IClientManager clientManager) {
        super(client);
        this.clientManager = clientManager;
    }

    @Override
    public void run() {

        UUID exchangeId = UUID.randomUUID();
        ClientDevice clientDevice = new ClientDevice(
                this.client.getUser().getUserName(),
                this.client.getClientDeviceId(),
                this.client.getPeerAddress()
        );

        logger.info("Creating request " + exchangeId);
        this.client.getObjectDataReplyHandler().addResponseCallbackHandler(exchangeId, this);
        IRequest dummyRequest;
        try {
            dummyRequest = new DummyRequest(exchangeId, clientDevice, this.clientManager.getClientLocations(this.client.getUser()));
        } catch (InputOutputException e) {
            logger.error("Can not get all client locations from the user");
            return;
        }

        try {
            super.sendRequest(dummyRequest);
        } catch (Exception e) {
            logger.error("Error in ANetworkHandler thread. Message: " + e.getMessage(), e);
            return;
        }
    }

    @Override
    public void onResponse(IResponse response) {
        super.onResponse(response);
    }

    @Override
    public Boolean getResult() {
        return super.isCompleted();
    }
}

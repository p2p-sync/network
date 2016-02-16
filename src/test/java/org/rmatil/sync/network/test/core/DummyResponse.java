package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;

import java.util.UUID;

public class DummyResponse implements IResponse {

    protected UUID         exchangeId;
    protected ClientDevice clientDevice;
    protected NodeLocation receiverAddress;

    public DummyResponse(UUID exchangeId, ClientDevice clientDevice, NodeLocation receiverAddress) {
        this.exchangeId = exchangeId;
        this.clientDevice = clientDevice;
        this.receiverAddress = receiverAddress;
    }

    @Override
    public UUID getExchangeId() {
        return this.exchangeId;
    }

    @Override
    public ClientDevice getClientDevice() {
        return this.clientDevice;
    }

    @Override
    public NodeLocation getReceiverAddress() {
        return this.receiverAddress;
    }

}

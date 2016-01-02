package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;

import java.util.UUID;

public class DummyResponse implements IResponse {

    protected UUID           exchangeId;
    protected ClientDevice   clientDevice;
    protected ClientLocation receiverAddress;

    public DummyResponse(UUID exchangeId, ClientDevice clientDevice, ClientLocation receiverAddress) {
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
    public ClientLocation getReceiverAddress() {
        return this.receiverAddress;
    }

}

package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.core.model.ClientDevice;

import java.util.UUID;

public class DummyRequest implements IRequest {

    protected UUID         exchangeId;
    protected ClientDevice clientDevice;

    public DummyRequest(UUID exchangeId, ClientDevice clientDevice) {
        this.exchangeId = exchangeId;
        this.clientDevice = clientDevice;
    }

    @Override
    public UUID getExchangeId() {
        return this.exchangeId;
    }

    @Override
    public ClientDevice getClientDevice() {
        return this.clientDevice;
    }
}

package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.model.ClientDevice;

import java.util.UUID;

public class DummyResponse implements IResponse {

    protected UUID         exchangeId;
    protected ClientDevice clientDevice;

    public DummyResponse(UUID exchangeId, ClientDevice clientDevice) {
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

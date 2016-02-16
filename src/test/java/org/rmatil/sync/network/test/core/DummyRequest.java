package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;

import java.util.List;
import java.util.UUID;

public class DummyRequest implements IRequest {

    protected List<NodeLocation> receiverAddresses;
    protected UUID               exchangeId;
    protected ClientDevice       clientDevice;

    public DummyRequest(UUID exchangeId, ClientDevice clientDevice, List<NodeLocation> receiverAddresses) {
        this.exchangeId = exchangeId;
        this.clientDevice = clientDevice;
        this.receiverAddresses = receiverAddresses;
    }

    @Override
    public List<NodeLocation> getReceiverAddresses() {
        return this.receiverAddresses;
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

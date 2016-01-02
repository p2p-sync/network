package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.ClientLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DummyRequest implements IRequest {

    protected IClient              client;
    protected List<ClientLocation> receiverAddresses;
    protected UUID                 exchangeId;
    protected ClientDevice         clientDevice;

    public DummyRequest(UUID exchangeId, ClientDevice clientDevice, List<ClientLocation> receiverAddresses) {
        this.exchangeId = exchangeId;
        this.clientDevice = clientDevice;
        this.receiverAddresses = receiverAddresses;
    }

    @Override
    public List<ClientLocation> getReceiverAddresses() {
        return this.receiverAddresses;
    }

    @Override
    public void setClient(IClient client) {
        this.client = client;
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
    public void sendResponse(IResponse response) {
        this.client.sendDirect(response.getReceiverAddress().getPeerAddress(), response);
    }

    @Override
    public void run() {
        IResponse response = new DummyResponse(
                this.exchangeId,
                new ClientDevice(
                        this.client.getUser().getUserName(),
                        this.client.getClientDeviceId(),
                        this.client.getPeerAddress()
                ),
                new ClientLocation(
                        this.clientDevice.getClientDeviceId(),
                        this.clientDevice.getPeerAddress()
                )
        );

        this.sendResponse(response);
    }
}

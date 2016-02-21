package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.INode;
import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.ANetworkHandler;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class DummyNetworkHandler extends ANetworkHandler<Boolean> {

    private final static Logger logger = LoggerFactory.getLogger(DummyNetworkHandler.class);

    protected INodeManager nodeManager;

    protected List<NodeLocation> nodeLocations;

    public DummyNetworkHandler(INode node, INodeManager nodeManager, List<NodeLocation> nodeLocations) {
        super(node);
        this.nodeManager = nodeManager;
        this.nodeLocations = nodeLocations;
    }

    @Override
    public void run() {
        try {
            UUID exchangeId = UUID.randomUUID();
            ClientDevice clientDevice = new ClientDevice(
                    super.node.getUser().getUserName(),
                    super.node.getClientDeviceId(),
                    super.node.getPeerAddress()
            );

            logger.info("Creating request " + exchangeId);
            super.node.getObjectDataReplyHandler().addResponseCallbackHandler(exchangeId, this);
            IRequest dummyRequest = new DummyRequest(exchangeId, clientDevice, this.nodeLocations);

            super.sendRequest(dummyRequest);
        } catch (Exception e) {
            logger.error("Error in ANetworkHandler thread. Message: " + e.getMessage(), e);
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

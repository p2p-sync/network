package org.rmatil.sync.network.test.core;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;

public class DummyObjectDataReply implements ObjectDataReply {

    protected int handledRequests = 0;

    @Override
    public Object reply(PeerAddress sender, Object request)
            throws Exception {
        if (request instanceof DummyRequest) {
            this.handledRequests++;
            return new DummyResponse(
                    ((DummyRequest) request).getExchangeId(),
                    ((DummyRequest) request).getClientDevice()
            );
        }

        return null;
    }

    public int getHandledRequests() {
        return handledRequests;
    }
}

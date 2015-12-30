package org.rmatil.sync.network.test.core;

import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.ANetworkHandler;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;

public class DummyNetworkHandler extends ANetworkHandler<Boolean> {

    public DummyNetworkHandler(IUser user, IClientManager clientManager, IClient client, IRequest request) {
        super(user, clientManager, client, request);
    }

    @Override
    protected Boolean handleResult()
            throws ConnectionFailedException {
        return this.notifiedClients.size() == this.respondedClients.size();
    }
}

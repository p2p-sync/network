package org.rmatil.sync.network.core;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.exception.ConnectionException;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.util.UUID;


public class Node implements INode {

    protected final static Logger logger = LoggerFactory.getLogger(Node.class);

    protected ConnectionConfiguration config;

    protected Connection connection;

    protected IUser user;

    protected UUID clientDeviceId;

    protected ObjectDataReplyHandler objectDataReplyHandler;

    protected INodeManager nodeManager;

    protected IUserManager userManager;

    protected IIdentifierManager<String, UUID> identifierManager;

    public Node(ConnectionConfiguration config, IUser user, UUID uuid) {
        this.config = config;
        this.user = user;
        this.clientDeviceId = uuid;
    }

    @Override
    public boolean start()
            throws ConnectionException, InvalidKeyException {
        return this.start(null, null);
    }


    @Override
    public boolean start(String bootstrapIpAddress, Integer bootstrapPort)
            throws ConnectionException, ConnectionFailedException, InvalidKeyException {

        this.connection = new Connection(this.config, this.objectDataReplyHandler);
        this.connection.open(this.user.getKeyPair());

        if (null != bootstrapIpAddress && null != bootstrapPort) {
            this.connection.connect(bootstrapIpAddress, bootstrapPort);
        }

        logger.info("Successfully started node on address " + this.getPeerAddress().inetAddress().getHostAddress() + ":" + this.config.getPort());

        IStorageAdapter dhtStorageAdapter = new DhtStorageAdapter(this.connection.getPeerDHT(), this.config.getCacheTtl());
        NodeLocation nodeLocation = new NodeLocation(
                this.clientDeviceId,
                this.connection.getPeerDHT().peerAddress()
        );

        this.nodeManager = new NodeManager(
                dhtStorageAdapter,
                Config.DEFAULT.getLocationsContentKey(),
                Config.DEFAULT.getPrivateKeyContentKey(),
                Config.DEFAULT.getPublicKeyContentKey(),
                Config.DEFAULT.getSaltContentKey(),
                Config.DEFAULT.getDomainKey()
        );

        this.identifierManager = new IdentifierManager(
                dhtStorageAdapter,
                this.user.getUserName(),
                Config.DEFAULT.getIdentifierContentKey(),
                Config.DEFAULT.getDomainKey()
        );

        this.userManager = new UserManager(
                this.nodeManager,
                nodeLocation
        );

        if (! this.userManager.login(this.user)) {
            logger.error("Failed to login the user " + this.user.getUserName());
            this.connection.close();
            return false;
        }

        logger.debug("Bootstrap peer succeeded to bootstrap at " + this.getPeerAddress().inetAddress().getHostAddress() + ":" + this.getPeerAddress().tcpPort());

        return true;
    }

    @Override
    public boolean shutdown() {
        if (null == this.connection || this.connection.isClosed()) {
            // reset the connection
            this.connection = null;

            return true;
        }

        logger.info("Shutting node down");

        // remove the node location
        this.userManager.logout(this.user);
        // friendly announce the shutdown of this node
        try {
            this.connection.close();
        } catch (ConnectionException e) {
            logger.error("Failed to shut down this node: " + e.getMessage());
            return false;
        }

        // reset the connection
        this.connection = null;

        return true;
    }

    @Override
    public boolean isConnected() {
        return null != this.connection && ! this.connection.isClosed();
    }

    @Override
    public void setObjectDataReplyHandler(ObjectDataReplyHandler objectDataReplyHandler) {
        if (null != this.connection) {
            throw new IllegalStateException("Can not set the object data reply handler after the connection has been set up");
        }

        // we are still able to set the object data reply
        this.objectDataReplyHandler = objectDataReplyHandler;
    }

    @Override
    public ObjectDataReplyHandler getObjectDataReplyHandler() {
        return this.objectDataReplyHandler;
    }

    @Override
    public UUID getClientDeviceId() {
        return clientDeviceId;
    }

    @Override
    public IUser getUser() {
        return user;
    }

    @Override
    public INodeManager getNodeManager() {
        return this.nodeManager;
    }

    @Override
    public IUserManager getUserManager() {
        return userManager;
    }

    @Override
    public IIdentifierManager<String, UUID> getIdentifierManager() {
        return this.identifierManager;
    }

    @Override
    public PeerAddress getPeerAddress() {
        return this.connection.getPeerDHT().peerAddress();
    }

    @Override
    public FutureDirect sendDirect(PeerAddress receiverAddress, Object dataToSend)
            throws ObjectSendFailedException {
        logger.trace("Sending object to peer with address " + receiverAddress.inetAddress().getHostAddress() + ":" + receiverAddress.tcpPort());

        return this.connection.sendDirect(receiverAddress, dataToSend);
    }
}

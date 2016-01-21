package org.rmatil.sync.network.core;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.ChannelClientConfiguration;
import net.tomp2p.connection.ChannelServerConfiguration;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;


public class Client implements IClient {

    /**
     * The maximum of concurrent connections we allow netty to use.
     * Using Integer.MAX_VALUE will result in a MemoryOverFlowException.
     *
     * @see <a href="https://github.com/p2p-sync/network/issues/3">https://github.com/p2p-sync/network/issues/3</a>
     */
    public final static int MAX_CONCURRENT_CONNECTIONS = 100000;

    protected final static Logger logger = LoggerFactory.getLogger(Client.class);

    protected final static Random rnd = new Random(42L);

    protected Config config;

    protected IUser user;

    protected UUID clientDeviceId;

    protected Bindings bindings;

    protected ObjectDataReplyHandler objectDataReplyHandler;

    protected PeerDHT peerDht;

    protected IClientManager locationManager;

    public Client(Config config, IUser user, UUID uuid) {
        this.config = config;
        this.user = user;
        this.clientDeviceId = uuid;
        this.bindings = new Bindings();
    }

    @Override
    public boolean start() {
        boolean success = this.initPeerDht();

        if (! success) {
            return false;
        }

        logger.info("Starting client on IP address " + this.getPeerAddress().inetAddress().getHostName() + ":" + this.config.getPort() + ". Inserting a new client location");

        IStorageAdapter dhtStorageAdapter = new DhtStorageAdapter(this.peerDht);
        ClientLocation clientLocation = new ClientLocation(this.clientDeviceId, this.peerDht.peerAddress());
        this.locationManager = new ClientManager(
                dhtStorageAdapter,
                this.config.getLocationsContentKey(),
                this.config.getPrivateKeyContentKey(),
                this.config.getLocationsContentKey(),
                this.config.getSaltContentKey(),
                this.config.getDomainKey()
        );

        try {
            this.locationManager.addClientLocation(this.user, clientLocation);
        } catch (InputOutputException e) {
            logger.error("Can not store my client location in the location manager. Message: " + e.getMessage());
            this.peerDht.shutdown();

            return false;
        }

        logger.debug("Bootstrap peer succeeded to bootstrap at " + this.getPeerAddress());

        return true;
    }

    @Override
    public boolean start(String bootstrapIpAdress, int bootstrapPort) {
        boolean success = this.initPeerDht();

        if (! success) {
            return false;
        }

        InetAddress address;
        try {
            // try to find client with the given location
            logger.debug("Trying to connect to " + bootstrapIpAdress + ":" + bootstrapPort + " ...");
            address = InetAddress.getByName(bootstrapIpAdress);
            FutureDiscover futureDiscover = this.peerDht
                    .peer()
                    .discover()
                    .inetAddress(address)
                    .ports(bootstrapPort)
                    .start();
            futureDiscover.awaitUninterruptibly();

            if (futureDiscover.isFailed()) {
                logger.error("Can not discover other client at address " + bootstrapIpAdress + ":" + bootstrapPort + ". Message: " + futureDiscover.failedReason());
                return false;
            }

            logger.debug("Connected. Trying to bootstrap...");

            // if found, try to bootstrap to it
            FutureBootstrap futureBootstrap = this.peerDht
                    .peer()
                    .bootstrap()
                    .inetAddress(address)
                    .ports(bootstrapPort)
                    .start();
            futureBootstrap.awaitUninterruptibly();

            if (futureBootstrap.isFailed()) {
                logger.error("Can not bootstrap to address " + bootstrapIpAdress + ". Message: " + futureBootstrap.failedReason());
                return false;
            }

            logger.info("Bootstrapping to client " + bootstrapIpAdress + ":" + bootstrapPort + " succeeded. My own address is now " + this.peerDht.peerAddress().inetAddress().getHostAddress() + ":" + this.peerDht.peerAddress().tcpPort());

            ClientLocation clientLocation = new ClientLocation(this.clientDeviceId, this.peerDht.peerAddress());
            IStorageAdapter dhtStorageAdapter = new DhtStorageAdapter(this.peerDht);
            this.locationManager = new ClientManager(
                    dhtStorageAdapter,
                    this.config.getLocationsContentKey(),
                    this.config.getPrivateKeyContentKey(),
                    this.config.getLocationsContentKey(),
                    this.config.getSaltContentKey(),
                    this.config.getDomainKey()
            );

            try {
                this.locationManager.addClientLocation(this.user, clientLocation);
            } catch (InputOutputException e) {
                logger.error("Can not store my client location in the location manager. Therefore, shutting down. Message: " + e.getMessage());
                this.peerDht.shutdown();

                return false;
            }

        } catch (UnknownHostException e) {
            logger.error("Can not start client. Message: " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean shutdown() {
        logger.info("Shutting client down...");

        if (null == this.peerDht) {
            return true;
        }

        BaseFuture future = this.peerDht.shutdown();
        future.awaitUninterruptibly();

        if (future.isSuccess()) {
            return true;
        }

        logger.error("Can not shut down client. Message: " + future.failedReason());

        return false;
    }

    @Override
    public void setObjectDataReplyHandler(ObjectDataReplyHandler objectDataReplyHandler) {
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
    public PeerAddress getPeerAddress() {
        return this.peerDht.peerAddress();
    }

    @Override
    public PeerDHT getPeerDht() {
        return this.peerDht;
    }

    protected boolean initPeerDht() {
        logger.debug("Setting up interface bindings");

        try {
            if (this.config.useIpV6()) {
                this.bindings.addProtocol(StandardProtocolFamily.INET6);
                this.bindings.addAddress(Inet4Address.getLocalHost());
            } else {
                this.bindings.addProtocol(StandardProtocolFamily.INET);
                this.bindings.addAddress(Inet6Address.getLocalHost());
            }
        } catch (UnknownHostException e) {
            logger.error("Can not add interface bindings to client. Message: " + e.getMessage());
            return false;
        }

        try {
            // limit of 10 concurrent connections
            ChannelServerConfiguration serverConfiguration = PeerBuilder.createDefaultChannelServerConfiguration();
            serverConfiguration.pipelineFilter(new PeerBuilder.EventExecutorGroupFilter(new DefaultEventExecutorGroup(MAX_CONCURRENT_CONNECTIONS)));

            ChannelClientConfiguration clientConfiguration = PeerBuilder.createDefaultChannelClientConfiguration();
            clientConfiguration.pipelineFilter(new PeerBuilder.EventExecutorGroupFilter(new DefaultEventExecutorGroup(MAX_CONCURRENT_CONNECTIONS)));

            this.peerDht = new PeerBuilderDHT(
                    new PeerBuilder(new Number160(rnd))
                            .channelServerConfiguration(serverConfiguration)
                            .channelClientConfiguration(clientConfiguration)
                            .keyPair(this.user.getKeyPair())
                            .ports(this.config.getPort())
                            .bindings(this.bindings)
                            .start()
            ).start();

            // TODO: https://github.com/p2p-sync/network/issues/2
            // TODO: 1. join network without public key
            // TODO: 2. fetch public key, private key, salt
            // TODO: 3. generate secret key for user with salt and pw
            // TODO: 4. add secret key to user
            // TODO: 5. decrypt private key with secret key from user
            // TODO: 6. set public-private keypair in the DHT


            if (null != this.objectDataReplyHandler) {
                logger.info("Setting object data reply...");
                this.peerDht.peer().objectDataReply(this.objectDataReplyHandler);
            }

        } catch (IOException e) {
            logger.error("Can not start peer dht. Message: " + e.getMessage());
            this.peerDht.shutdown();

            return false;
        }

        this.peerDht.storageLayer().protection(
                this.config.getProtectionDomainEnable(),
                this.config.getProtectionDomainMode(),
                this.config.getProtectionEntryEnable(),
                this.config.getProtectionEntryMode()
        );

        return true;
    }

    @Override
    public FutureDirect sendDirect(PeerAddress receiverAddress, Object dataToSend)
            throws ObjectSendFailedException {
        logger.trace("Sending object to peer with address " + receiverAddress.inetAddress().getHostAddress() + ":" + receiverAddress.tcpPort());
        // TODO: sign & encrypt files
        return this.peerDht.peer().sendDirect(receiverAddress).object(dataToSend).start();
    }
}

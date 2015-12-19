package org.rmatil.sync.network.core;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
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


public class Client implements IClient {

    protected final static Logger logger = LoggerFactory.getLogger(Client.class);

    protected final static Random rnd = new Random(42L);

    protected Config config;

    protected IUser user;

    protected Bindings bindings;

    protected ObjectDataReply objectDataReply;

    protected PeerDHT peerDht;

    protected IClientManager locationManager;

    public Client(Config config, IUser user) {
        this.config = config;
        this.user = user;
        this.bindings = new Bindings();
    }

    public void setObjectDataReply(ObjectDataReply objectDataReply) {
        this.objectDataReply = objectDataReply;
    }

    @Override
    public boolean start() {
        boolean success = this.initPeerDht();

        if (! success) {
            return false;
        }

        logger.info("Starting client on IP address " + this.getPeerAddress().inetAddress().getHostName() + ":" + this.config.getPort() + ". Inserting a new client location");

        IStorageAdapter dhtStorageAdapter = new DhtStorageAdapter(this.peerDht);
        ClientLocation clientLocation = new ClientLocation(this.peerDht.peerAddress());
        this.locationManager = new ClientManager(
                dhtStorageAdapter,
                this.config.getLocationsContentKey(),
                this.config.getPrivateKeyContentKey(),
                this.config.getLocationsContentKey(),
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
    public boolean start(ClientLocation bootstrapLocation) {
        boolean success = this.initPeerDht();

        if (! success) {
            return false;
        }

        InetAddress address;
        try {
            // try to find client with the given location
            logger.debug("Trying to connect to " + bootstrapLocation.getIpAddress() + ":" + bootstrapLocation.getPort() + " ...");
            address = InetAddress.getByName(bootstrapLocation.getIpAddress());
            FutureDiscover futureDiscover = this.peerDht
                    .peer()
                    .discover()
                    .inetAddress(address)
                    .ports(bootstrapLocation.getPort())
                    .start();
            futureDiscover.awaitUninterruptibly();

            if (futureDiscover.isFailed()) {
                logger.error("Can not discover other client at address " + bootstrapLocation.getIpAddress() + ":" + bootstrapLocation.getPort() + ". Message: " + futureDiscover.failedReason());
                return false;
            }

            logger.debug("Connected. Trying to bootstrap...");

            // if found, try to bootstrap to it
            FutureBootstrap futureBootstrap = this.peerDht
                    .peer()
                    .bootstrap()
                    .inetAddress(address)
                    .ports(bootstrapLocation.getPort())
                    .start();
            futureBootstrap.awaitUninterruptibly();

            if (futureBootstrap.isFailed()) {
                logger.error("Can not bootstrap to address " + bootstrapLocation.getIpAddress() + ". Message: " + futureBootstrap.failedReason());
                return false;
            }

            logger.info("Bootstrapping of client " + bootstrapLocation.getIpAddress() + ":" + bootstrapLocation.getPort() + " succeeded");

        } catch (UnknownHostException e) {
            logger.error("Can not start client. Message: " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean shutdown() {
        logger.debug("Shutting client down...");

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

    public PeerAddress getPeerAddress() {
        return this.peerDht.peerAddress();
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
            this.peerDht = new PeerBuilderDHT(
                    new PeerBuilder(new Number160(rnd))
                            .keyPair(this.user.getKeyPair())
                            .ports(this.config.getPort())
                            .bindings(this.bindings)
                            .start()
            ).start();

            if (null != this.objectDataReply) {
                logger.info("Setting object data reply...");
                this.peerDht.peer().objectDataReply(this.objectDataReply);
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

    public void sendDirect() {
        // get peer address from DHT
        PeerAddress peerAddress = null;
        this.peerDht.peer().sendDirect(peerAddress).object("hallo");
        this.peerDht.peer().objectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object request)
                    throws Exception {
                return null;
            }
        });
    }
}

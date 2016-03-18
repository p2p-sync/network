package org.rmatil.sync.network.core;

import io.netty.util.concurrent.DefaultEventExecutorGroup;
import net.tomp2p.connection.*;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.core.exception.ConnectionException;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.messaging.EncryptedDataReplyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Use this class to open resp. close connections between
 * peers.
 */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    /**
     * The maximum of concurrent connections we allow netty to use.
     * Using Integer.MAX_VALUE will result in a MemoryOverFlowException.
     *
     * @see <a href="https://github.com/p2p-sync/network/issues/3">https://github.com/p2p-sync/network/issues/3</a>
     */
    public static final int MAX_CONCURRENT_CONNECTIONS = 80;

    /**
     * The lowes port which could be used, if available.
     * Note, that ports below this threshold are reserved
     * for privileged services.
     */
    public static final int MIN_PORT_NUMBER = 1024;

    /**
     * The highest port which could be used, if available.
     * (unsigned 16-bit integer: 2^16)
     */
    public static final int MAX_PORT_NUMBER = 65535;

    protected PeerDHT peerDHT;

    protected ConnectionConfiguration config;

    protected EncryptedDataReplyHandler encryptedDataReplyHandler;

    public static boolean isPortAvailable(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid port number " + port + ". Port must be between " + MIN_PORT_NUMBER + " and " + MAX_PORT_NUMBER);
        }

        ServerSocket serverSocket = null;
        DatagramSocket datagramSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // port is already in use
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }

            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    // if an error occurred during closing the socket again...
                }
            }
        }

        return false;
    }

    public static int getNextFreePort(int port) {
        try {
            port++;

            while (! Connection.isPortAvailable(port)) {
                port++;
            }
        } catch (IllegalArgumentException e) {
            throw new ConnectionException("No free port is available");
        }

        return port;
    }

    /**
     * @param config                    The connection configuration
     * @param encryptedDataReplyHandler The object data reply handler which is attached to the peer when opening the connection
     */
    public Connection(ConnectionConfiguration config, EncryptedDataReplyHandler encryptedDataReplyHandler) {
        this.config = config;
        this.encryptedDataReplyHandler = encryptedDataReplyHandler;
    }

    /**
     * Open a new connection, i.e. initialising a new node using
     * the given keypair for domain protected values.
     *
     * @param keyPair The keypair to use for domain protected values. Note that this must be the same for all clients of the same user
     *
     * @throws ConnectionException If no free port could have been allocated to start this node or another error occurred during start up
     * @throws InvalidKeyException If the given keypair does not have a RSA public and private key
     */
    public void open(KeyPair keyPair)
            throws ConnectionException, InvalidKeyException {

        if (null == keyPair || null == keyPair.getPrivate() || null == keyPair.getPublic()) {
            throw new InvalidKeyException("A RSA KeyPair must be provided");
        }

        if (! (keyPair.getPublic() instanceof RSAPublicKey)) {
            throw new InvalidKeyException("The public key must be a RSA public key");
        }

        if (! (keyPair.getPrivate() instanceof RSAPrivateKey)) {
            throw new InvalidKeyException("The private key must be a RSA private key");
        }

        int port = this.config.getPort();

        if (! Connection.isPortAvailable(port)) {
            logger.debug("Provided port " + port + " is already in use. Looking for the next free one");

            // this throws a ConnectionException, if no port is available
            port = Connection.getNextFreePort(port);
        }

        logger.debug("Setting node up at free port " + port);

        // see https://github.com/Hive2Hive/Hive2Hive/issues/117
        Bindings bindings = new Bindings().listenAny();

        ChannelServerConfiguration serverConfiguration = PeerBuilder.createDefaultChannelServerConfiguration();
        serverConfiguration.signatureFactory(new RSASignatureFactory());
        serverConfiguration.pipelineFilter(new PeerBuilder.EventExecutorGroupFilter(new DefaultEventExecutorGroup(MAX_CONCURRENT_CONNECTIONS)));
        serverConfiguration.ports(new Ports(port, port));

        ChannelClientConfiguration clientConfiguration = PeerBuilder.createDefaultChannelClientConfiguration();
        clientConfiguration.signatureFactory(new RSASignatureFactory());
        clientConfiguration.pipelineFilter(new PeerBuilder.EventExecutorGroupFilter(new DefaultEventExecutorGroup(MAX_CONCURRENT_CONNECTIONS)));

        PeerBuilder peerBuilder = new PeerBuilder(Number160.createHash(this.config.getNodeId()))
                .ports(port)
                .keyPair(keyPair) // note, that this enables signing of messages by default: @see Message#isSign()
                .bindings(bindings)
                .channelServerConfiguration(serverConfiguration)
                .channelClientConfiguration(clientConfiguration);

        try {
            this.peerDHT = new PeerBuilderDHT(peerBuilder.start()).start();
        } catch (IOException e) {
            this.close();

            throw new ConnectionException("Failed to start peer", e);
        }

        if (this.config.isFirewalled()) {
            PeerAddress peerAddress = this.peerDHT
                    .peerAddress()
                    .changeFirewalledTCP(true)
                    .changeFirewalledUDP(true);

            peerDHT.peer().peerBean().serverPeerAddress(peerAddress);
        }

        // TODO: https://github.com/p2p-sync/network/issues/2
        // TODO: 1. join network without public key
        // TODO: 2. fetch public key, private key, salt
        // TODO: 3. generate secret key for user with salt and pw
        // TODO: 4. add secret key to user
        // TODO: 5. decrypt private key with secret key from user
        // TODO: 6. set public-private keypair in the DHT

        if (null != this.encryptedDataReplyHandler) {
            logger.info("Setting ObjectDataReplyHandler " + this.encryptedDataReplyHandler.getClass().getName());
            this.peerDHT.peer().objectDataReply(this.encryptedDataReplyHandler);
        }

        // set storage layer protection
        this.peerDHT.storageLayer().protection(
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.NO_MASTER,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.NO_MASTER
        );

    }

    /**
     * Connect this peer to the node residing at the given bootstrap location.
     *
     * @param bootstrapIpAddress The ip address to use to bootstrap to
     * @param bootstrapPort      The port to bootstrap to
     *
     * @throws ConnectionFailedException If bootstrapping to the other node failed
     * @throws IllegalStateException     If the connection is not yet opened
     */
    public void connect(String bootstrapIpAddress, int bootstrapPort)
            throws ConnectionFailedException, IllegalStateException {
        if (null == this.peerDHT) {
            throw new IllegalStateException("Open the connection first");
        }

        logger.info("Trying to connect to " +
                bootstrapIpAddress +
                ":" +
                bootstrapPort
        );

        InetAddress address;
        try {
            address = InetAddress.getByName(bootstrapIpAddress);
        } catch (UnknownHostException e) {
            this.close();

            throw new ConnectionFailedException("Could not get inet address for provided bootstrap address", e);
        }

        FutureDiscover futureDiscover = this.peerDHT
                .peer()
                .discover()
                .discoverTimeoutSec((int) Math.ceil(this.config.getPeerDiscoveryTimeout() / 1000L))
                .inetAddress(address)
                .ports(bootstrapPort)
                .start();

        futureDiscover.awaitUninterruptibly();

        if (futureDiscover.isFailed()) {

            if (this.config.isFirewalled) {
                // TODO: implement https://github.com/p2p-sync/network/issues/6
                logger.warn("Firewall support is not implemented yet");
            }

            this.close();

            throw new ConnectionFailedException("Can not discover other peer at " +
                    bootstrapIpAddress +
                    ":" +
                    bootstrapPort +
                    ": " +
                    futureDiscover.failedReason()
            );
        }

        logger.debug("Peer discovery was successful");

        FutureBootstrap futureBootstrap = this.peerDHT
                .peer()
                .bootstrap()
                .inetAddress(address)
                .ports(bootstrapPort)
                .start();

        futureBootstrap.awaitUninterruptibly(this.config.getPeerBootstrapTimeout());

        if (futureBootstrap.isFailed()) {
            this.close();

            throw new ConnectionFailedException("Can not bootstrap to other peer at " +
                    bootstrapIpAddress +
                    ":" +
                    bootstrapPort +
                    ": " +
                    futureBootstrap.failedReason()
            );
        }

        logger.debug("Bootstrap succeeded");
    }

    /**
     * Send the given data to the specified receiver
     *
     * @param receiverAddress The address to which to send the data
     * @param dataToSend      The data to send
     *
     * @return The future
     */
    public FutureDirect sendDirect(PeerAddress receiverAddress, Object dataToSend) {
        return this.peerDHT
                .peer()
                .sendDirect(receiverAddress)
                .object(dataToSend)
                .start();
    }

    /**
     * Close the connection of this peer in means of a friendly (i.e. announced)
     * shutdown.
     *
     * @throws ConnectionException If waiting for shutdown timed out
     */
    public void close()
            throws ConnectionException {
        if (null == this.peerDHT) {
            logger.trace("Can not shutdown peer: PeerDHT was not initialized yet");
            return;
        }

        if (this.peerDHT.peer().isShutdown()) {
            logger.trace("Can not shutdown peer: PeerDHT is already shut down");
            return;
        }

        // notify the shutdown to next neighbours
        boolean announceSuccessful = this.peerDHT.peer()
                .announceShutdown()
                .start()
                .awaitUninterruptibly(this.config.getShutdownAnnounceTimeout());

        if (! announceSuccessful) {
            logger.debug("Shutdown announce was not yet completed. Shutting node down now");
        }

        BaseFuture shutdownFuture = this.peerDHT.shutdown();

        if (shutdownFuture.isFailed()) {
            throw new ConnectionException("Failed to shut down node: " + shutdownFuture.failedReason());
        }

        logger.trace("Shutdown of peer succeeded");
    }

    /**
     * Returns true, if this connection is closed, i.e. no peer is started.
     *
     * @return True, if closed, false otherwise
     */
    public boolean isClosed() {
        // returns true, if no peer dht is specified or already shutdown
        return (null != this.peerDHT && this.peerDHT.peer().isShutdown()) || null == this.peerDHT;
    }

    /**
     * Returns the peer DHT backed by this connection
     *
     * @return The peer dht
     */
    public PeerDHT getPeerDHT() {
        return peerDHT;
    }
}

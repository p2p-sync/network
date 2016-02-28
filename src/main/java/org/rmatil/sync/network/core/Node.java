package org.rmatil.sync.network.core;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.exception.ConnectionException;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.messaging.EncryptedDataReplyHandler;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.EncryptedData;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.security.encryption.asymmetric.rsa.RsaEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesKeyFactory;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
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

    protected RsaEncryption rsaEncryption;
    protected AesEncryption aesEncryption;

    public Node(ConnectionConfiguration config, IUser user, UUID uuid) {
        this.config = config;
        this.user = user;
        this.clientDeviceId = uuid;
        this.rsaEncryption = new RsaEncryption();
        this.aesEncryption = new AesEncryption();
    }

    @Override
    public boolean start()
            throws ConnectionException, InvalidKeyException {
        return this.start(null, null);
    }


    @Override
    public boolean start(String bootstrapIpAddress, Integer bootstrapPort)
            throws ConnectionException, ConnectionFailedException, InvalidKeyException {

        this.connection = new Connection(
                this.config,
                new EncryptedDataReplyHandler(
                        this.objectDataReplyHandler,
                        this.nodeManager,
                        (RSAPrivateKey) this.user.getPrivateKey()
                )
        );

        this.connection.open(this.user.getKeyPair());

        if (null != bootstrapIpAddress && null != bootstrapPort) {
            this.connection.connect(bootstrapIpAddress, bootstrapPort);
        }

        logger.info("Successfully started node on address " + this.getPeerAddress().inetAddress().getHostAddress() + ":" + this.config.getPort());

        IStorageAdapter dhtStorageAdapter = new DhtStorageAdapter(this.connection.getPeerDHT(), this.config.getCacheTtl());
        NodeLocation nodeLocation = new NodeLocation(
                this.user.getUserName(),
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

        this.userManager = new UserManager(this.nodeManager);

        if (! this.userManager.login(this.user, nodeLocation)) {
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
        NodeLocation nodeLocation = new NodeLocation(
                this.user.getUserName(),
                this.clientDeviceId,
                this.connection.getPeerDHT().peerAddress()
        );

        this.userManager.logout(this.user, nodeLocation);
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
    public FutureDirect sendDirect(NodeLocation receiverAddress, Object data) {
        logger.info("Sending request to "
                + receiverAddress.getUsername()
                + " ("
                + receiverAddress.getIpAddress()
                + ":"
                + receiverAddress.getPort()
                + ")"
        );

        // get public key from receiver to encrypt
        RSAPublicKey publicKey;
        try {
            publicKey = (RSAPublicKey) this.nodeManager.getPublicKey(receiverAddress.getUsername());
        } catch (InputOutputException e) {
            throw new ObjectSendFailedException(
                    "Could not use public key of user "
                            + receiverAddress.getUsername()
                            + " to encrypt data. Aborting to send request for this receiver. Message: "
                            + e.getMessage()
            );
        }

        if (null == publicKey) {
            throw new ObjectSendFailedException("Can not encrypt message. No public key found for receiver " + receiverAddress.getUsername());
        }

        try {
            // encrypt the actual data using the AES key
            byte[] initVector = AesEncryption.generateInitializationVector();
            SecretKey aesKey = AesKeyFactory.generateSecretKey();

            byte[] aesEncryptedData = this.aesEncryption.encrypt(aesKey, initVector, ByteSerializer.toBytes(data));

            // encrypt the AES key with RSA
            byte[] encodedAesKey = aesKey.getEncoded();
            byte[] symmetricKey = new byte[AesEncryption.INIT_VECTOR_LENGTH + encodedAesKey.length];

            System.arraycopy(initVector, 0, symmetricKey, 0, initVector.length);
            System.arraycopy(encodedAesKey, 0, symmetricKey, initVector.length, encodedAesKey.length);

            byte[] rsaEncryptedData = this.rsaEncryption.encrypt(publicKey, symmetricKey);

            return this.connection.sendDirect(receiverAddress.getPeerAddress(), new EncryptedData(rsaEncryptedData, aesEncryptedData));
        } catch (IOException | SecurityException e) {
            throw new ObjectSendFailedException(
                    "Failed to encrypt data for receiver "
                            + receiverAddress.getUsername()
                            + " ("
                            + receiverAddress.getIpAddress()
                            + ":"
                            + receiverAddress.getPort()
                            + "). Aborting to send request for this receiver. Message: "
                            + e.getMessage()
            );
        }
    }
}

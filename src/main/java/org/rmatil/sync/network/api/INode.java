package org.rmatil.sync.network.api;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.core.exception.ConnectionException;
import org.rmatil.sync.network.core.exception.ConnectionFailedException;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.NodeLocation;

import java.security.InvalidKeyException;
import java.util.UUID;

/**
 * A node of a particular user
 */
public interface INode {

    /**
     * Start the node as bootstrap peer
     *
     * @return True, if starting as bootstrap peer succeeded, false otherwise
     *
     * @throws ConnectionException If creating this node failed
     * @throws InvalidKeyException If the keypair of the user is not a RSA keypair or the public resp. private key is missing
     */
    boolean start()
            throws ConnectionException, InvalidKeyException;

    /**
     * Start the node and let it connect to an online peer
     * at the given node location
     *
     * @param bootstrapIpAddress The ip address of an online node to which to bootstrap to
     * @param bootstrapPort      The port of an online node to which to bootstrap to
     *
     * @return True, if starting and connecting succeeded, false otherwise
     *
     * @throws ConnectionException       If creating this node failed
     * @throws ConnectionFailedException If connecting to the other peer failed
     * @throws InvalidKeyException       If the keypair of the user is not a RSA keypair or the public resp. private key is missing
     */
    boolean start(String bootstrapIpAddress, Integer bootstrapPort)
            throws ConnectionException, ConnectionFailedException, InvalidKeyException;

    /**
     * Shuts down the node. Blocks until success or failure.
     *
     * @return True, if the node could have been shut down, false otherwise
     */
    boolean shutdown();

    /**
     * Returns true if this node is connected
     *
     * @return True, if connected. False otherwise
     */
    boolean isConnected();

    /**
     * Sets the object data reply handler which should be invoked
     * when an object is received by the peer
     *
     * @param objectDataReplyHandler The data reply handler to add
     */
    void setObjectDataReplyHandler(ObjectDataReplyHandler objectDataReplyHandler);

    /**
     * Returns the object data reply handler which handles all incoming requests
     *
     * @return The object data handler
     */
    ObjectDataReplyHandler getObjectDataReplyHandler();

    /**
     * Returns the uuid of this node's device
     *
     * @return The uuid of this device
     */
    UUID getClientDeviceId();

    /**
     * Returns the user corresponding to this node
     *
     * @return The user of this node
     */
    IUser getUser();

    /**
     * Returns the node manager used.
     * <p color="red">This is only initialised after starting the node</p>
     *
     * @return The node manager
     */
    INodeManager getNodeManager();

    /**
     * Returns the user manager used.
     * <p color="red">This is only initialised after starting the node</p>
     *
     * @return The user manager
     */
    IUserManager getUserManager();

    /**
     * Returns the identifier manager used.
     * <p color="red">This is only initialised after starting the node</p>
     *
     * @return The identifier manager
     */
    IIdentifierManager<String, UUID> getIdentifierManager();

    /**
     * Returns the peer address of the node
     *
     * @return The peer address
     */
    PeerAddress getPeerAddress();

    /**
     * Sends the given object to the specified peer address.
     * Note, that you still have to call {@link FutureDirect#await()} on it.
     *
     * @param receiverAddress The location of the node to which the object should be sent
     * @param data            The data to send to the node
     *
     * @return The Future. Note, that you have to await until this future is complete
     *
     * @throws ObjectSendFailedException If sending the object to the node failed
     */
    FutureDirect sendDirect(NodeLocation receiverAddress, Object data)
            throws ObjectSendFailedException;

}

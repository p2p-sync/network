package org.rmatil.sync.network.api;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;

import java.util.UUID;

/**
 * A client of a particular user
 */
public interface IClient {

    /**
     * Start the client as bootstrap peer
     *
     * @return True, if starting as bootstrap peer succeeded, false otherwise
     */
    boolean start();

    /**
     * Start the client and let it connect to an online peer
     * at the given client location
     *
     * @param bootstrapIpAddress The ip address of an online client to which to bootstrap to
     * @param bootstrapPort      The port of an online client to which to bootstrap to
     *
     * @return True, if starting and connecting succeeded, false otherwise
     */
    boolean start(String bootstrapIpAddress, int bootstrapPort);

    /**
     * Shuts down the client. Blocks until success or failure.
     *
     * @return True, if the client could have been shut down, false otherwise
     */
    boolean shutdown();

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
     * Returns the uuid of this client's device
     *
     * @return The uuid of this device
     */
    UUID getClientDeviceId();

    /**
     * Returns the user corresponding to this client
     *
     * @return The user of this client
     */
    IUser getUser();

    /**
     * Returns the client manager used.
     * <p color="red">This is only initialised after starting the client</p>
     *
     * @return The client manager
     */
    IClientManager getClientManager();

    /**
     * Returns the identifier manager used.
     * <p color="red">This is only initialised after starting the client</p>
     *
     * @return The identifier manager
     */
    IIdentifierManager<String, UUID> getIdentifierManager();

    /**
     * Returns the peer address of the client
     *
     * @return The peer address
     */
    PeerAddress getPeerAddress();

    /**
     * Returns the peer DHT of the client
     *
     * @return The peer DHT
     */
    PeerDHT getPeerDht();

    /**
     * Sends the given object to the specified peer address.
     * Note, that you still have to call {@link FutureDirect#await()} on it.
     *
     * @param receiverAddress The address of the client to which the object should be sent
     * @param dataToSend      The data to send to the client
     *
     * @return The Future. Note, that you have to await until this future is complete
     *
     * @throws ObjectSendFailedException If sending the object to the client failed
     */
    FutureDirect sendDirect(PeerAddress receiverAddress, Object dataToSend)
            throws ObjectSendFailedException;

}
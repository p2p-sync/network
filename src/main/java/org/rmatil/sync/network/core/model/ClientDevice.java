package org.rmatil.sync.network.core.model;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.UUID;

/**
 * A wrapper for representing some particular data
 * about the node of a user.
 */
public class ClientDevice implements Serializable {

    /**
     * The user name of the user to which this node belongs
     */
    protected String userName;

    /**
     * The unique id of this node
     */
    protected UUID clientDeviceId;

    /**
     * The address of this node
     */
    protected PeerAddress peerAddress;

    /**
     * @param userName       The user name of the user to which this node belongs
     * @param clientDeviceId The unique node id
     * @param peerAddress    The address of the node
     */
    public ClientDevice(String userName, UUID clientDeviceId, PeerAddress peerAddress) {
        this.userName = userName;
        this.clientDeviceId = clientDeviceId;
        this.peerAddress = peerAddress;
    }

    /**
     * Returns the user name of the user to which this node belongs
     *
     * @return The user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * The unique node device id
     *
     * @return The node id
     */
    public UUID getClientDeviceId() {
        return clientDeviceId;
    }

    /**
     * The address of this node device
     *
     * @return The address of this node
     */
    public PeerAddress getPeerAddress() {
        return peerAddress;
    }

    /**
     * Sets the node's address
     *
     * @param peerAddress The address to set
     */
    public void setPeerAddress(PeerAddress peerAddress) {
        this.peerAddress = peerAddress;
    }
}

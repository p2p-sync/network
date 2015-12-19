package org.rmatil.sync.network.api;

import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.core.model.ClientLocation;

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
     * @param bootstrapLocation The client location of an online peer
     *
     * @return True, if starting and connecting succeeded, false otherwise
     */
    boolean start(ClientLocation bootstrapLocation);

    /**
     * Shuts down the client. Blocks until success or failure.
     *
     * @return True, if the client could have been shut down, false otherwise
     */
    boolean shutdown();

    /**
     * Returns the peer address of the client
     *
     * @return The peer address
     */
    PeerAddress getPeerAddress();

}
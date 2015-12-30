package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.exception.ConnectionFailedException;

import java.util.concurrent.Callable;

/**
 * Handles the protocol implementation between clients to exchange
 * information like files, meta information and more.
 * <p>
 * Must implement Callable to be able to be run asynchronously.
 *
 * @param <T> The result type which should be returned, once the information exchange is completed and the protocol steps are performed
 */
public interface INetworkHandler<T> extends Callable<T> {

    /**
     * The main method which starts the protocol implementation.
     * <p>
     * {@inheritDoc}
     *
     * @return The result after all protocol steps are performed. May be null
     */
    T call();

    /**
     * Sends the request of the network handler to all online peers of the client.
     *
     * @throws ConnectionFailedException If the other online clients can not be determined
     */
    void sendRequest()
            throws ConnectionFailedException;

    /**
     * Returns true, once all necessary clients have responded
     *
     * @return True, if all necessary clients habe been responded. False otherwise.
     */
    boolean waitForNotifiedClients();

}

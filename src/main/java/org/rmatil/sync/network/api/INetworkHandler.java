package org.rmatil.sync.network.api;

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
     * Returns true, once all necessary clients have responded
     *
     * @return True, if all necessary clients habe been responded. False otherwise.
     */
    boolean waitForNotifiedClients();

}

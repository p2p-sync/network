package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.exception.ConnectionFailedException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Handles the protocol implementation between clients to exchange
 * information like files, meta information and more.
 * <p>
 * Must implement Callable to be able to be run asynchronously.
 *
 * @param <T> The result type which should be returned, once the information exchange is completed and the protocol steps are performed
 */
public interface INetworkHandler<T> extends Runnable {

    /**
     * The main method which starts the protocol implementation.
     * <p>
     * {@inheritDoc}
     */
    void run();

    /**
     * Sends the request of the network handler to all online peers of the client.
     *
     * @throws ConnectionFailedException If the other online clients can not be determined
     */
    void sendRequest(IRequest request)
            throws ConnectionFailedException;

    /**
     * Blocks until all notified clients have responded
     *
     * @throws InterruptedException If the thread got interrupted while waiting
     */
    void await()
            throws InterruptedException;

    /**
     * Blocks for the given timeout.
     *
     * @param timeout The timeout to wait
     * @param timeUnit The time unit which qualifies the timeout
     *
     * @throws InterruptedException If the thread got interrupted while waiting
     */
    void await(long timeout, TimeUnit timeUnit)
            throws InterruptedException;

    /**
     * Returns true once all notified clients have responded.
     *
     * @return True, if all notified clients have responded, false otherwise.
     */
    boolean isCompleted();

    /**
     * Returns the progress until this handler's communication
     * is considered complete (See {@link INetworkHandler#isCompleted()}).
     *
     * Returns values between 100 and 0 (inclusive).
     *
     * @return The progress
     */
    int getProgress();


    /**
     * Returns the final result of the exchange.
     * Note, that the result may be null before all clients have responded.
     *
     * @return The result once the all necessary clients responded. May be null before all clients have responded
     */
    T getResult();
}

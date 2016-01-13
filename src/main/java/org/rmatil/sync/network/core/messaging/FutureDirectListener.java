package org.rmatil.sync.network.core.messaging;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link FutureDirect} listener for waiting for a
 * future direct to complete.
 * It can be used by invoking {@link FutureDirect#addListener(BaseFutureListener)} on
 * the future object.
 */
public class FutureDirectListener extends BaseFutureAdapter<FutureDirect> {

    /**
     * The maximum timeout to wait for the completion to end
     */
    public static final long MAX_TIMEOUT = 30000L;

    /**
     * The countdown latch to use for waiting
     */
    protected CountDownLatch countDownLatch;

    public FutureDirectListener() {
        this.countDownLatch = new CountDownLatch(1);
    }

    /**
     * Waits a maximum timeout of {@link FutureDirectListener#MAX_TIMEOUT}
     * for the future to complete.
     *
     * @throws InterruptedException If the thread got interrupted while waiting
     */
    public void await()
            throws InterruptedException {
        this.countDownLatch.await(MAX_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public void operationComplete(FutureDirect future)
            throws Exception {
        this.countDownLatch.countDown();
    }
}

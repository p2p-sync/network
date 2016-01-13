package org.rmatil.sync.network.test.core.messaging;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.core.messaging.FutureDirectListener;

import java.util.concurrent.*;

public class FutureDirectListenerTest {

    protected static FutureDirectListener futureDirectListener;

    @BeforeClass
    public static void setUp() {
        futureDirectListener = new FutureDirectListener();
    }

    @Test
    public void test()
            throws InterruptedException {
        Runnable runnable = () -> {
            try {
                futureDirectListener.operationComplete(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(runnable, 2000L, TimeUnit.MILLISECONDS);

        // await the 2000 milliseconds
        futureDirectListener.await();
    }

}

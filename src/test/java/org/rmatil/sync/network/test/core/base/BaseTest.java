package org.rmatil.sync.network.test.core.base;

import org.rmatil.sync.network.core.ConnectionConfiguration;

public class BaseTest {

    public static ConnectionConfiguration getTestConfig1() {
        return new ConnectionConfiguration(
                "node1",
                4003,
                0L,
                5000L,
                5000L,
                5000L,
                false
        );
    }

    public static ConnectionConfiguration getTestConfig2() {
        return new ConnectionConfiguration(
                "node2",
                4004,
                0L,
                5000L,
                5000L,
                5000L,
                false
        );
    }

    public static ConnectionConfiguration getTestConfig3() {
        return new ConnectionConfiguration(
                "node3",
                4005,
                0L,
                5000L,
                5000L,
                5000L,
                false
        );
    }
}

package org.rmatil.sync.network.test.core.config;

public enum Config {

    DEFAULT("LOCATION");

    private String locationsLocationKey;

    private String testIpV4Address = "127.0.0.1";
    private int    testPort        = 4001;


    Config(String locattionsLocationKey) {
        this.locationsLocationKey = locattionsLocationKey;
    }

    public String getLocationsLocationKey() {
        return locationsLocationKey;
    }

    public String getTestIpV4Address() {
        return testIpV4Address;
    }

    public int getTestPort() {
        return testPort;
    }
}

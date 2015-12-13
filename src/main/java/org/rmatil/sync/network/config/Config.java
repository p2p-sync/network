package org.rmatil.sync.network.config;

public enum Config {

    DEFAULT("LOCATION");

    private String locationsLocationKey;

    Config(String locattionsLocationKey) {
        this.locationsLocationKey = locattionsLocationKey;
    }

    public String getLocationsLocationKey() {
        return locationsLocationKey;
    }
}

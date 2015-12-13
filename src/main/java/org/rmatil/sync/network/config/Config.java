package org.rmatil.sync.network.config;

public enum Config {

    DEFAULT("LOCATION");

    private String locationsContentKey;

    Config(String locattionsLocationKey) {
        this.locationsContentKey = locattionsLocationKey;
    }

    public String getLocationsContentKey() {
        return locationsContentKey;
    }
}

package org.rmatil.sync.network.config;

import net.tomp2p.dht.StorageLayer;

public enum Config {

    DEFAULT("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", 4001, false, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv6("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", 4002, false, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv4("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", 4003, false, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv4_2("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", 4004, false, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv4_3("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", 4005, false, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER);

    private String locationsContentKey;

    private String privateKeyContentKey;

    private String publicKeyContentKey;

    private String domainKey;

    private int port;

    private boolean useIpV6;

    private StorageLayer.ProtectionEnable protectionDomainEnable;
    private StorageLayer.ProtectionMode   protectionDomainMode;
    private StorageLayer.ProtectionEnable protectionEntryEnable;
    private StorageLayer.ProtectionMode   protectionEntryMode;

    Config(String locationsLocationKey,
           String privateKeyContentKey,
           String publicKeyContentKey,
           String domainKey,
           int port,
           boolean useIpV6,
           StorageLayer.ProtectionEnable protectionDomainEnable,
           StorageLayer.ProtectionMode protectionDomainMode,
           StorageLayer.ProtectionEnable protectionEntryEnable,
           StorageLayer.ProtectionMode protectionEntryMode
    ) {
        this.locationsContentKey = locationsLocationKey;
        this.privateKeyContentKey = privateKeyContentKey;
        this.publicKeyContentKey = publicKeyContentKey;
        this.domainKey = domainKey;
        this.port = port;
        this.useIpV6 = useIpV6;
        this.protectionDomainEnable = protectionDomainEnable;
        this.protectionDomainMode = protectionDomainMode;
        this.protectionEntryEnable = protectionEntryEnable;
        this.protectionEntryMode = protectionEntryMode;
    }

    public String getLocationsContentKey() {
        return locationsContentKey;
    }

    public String getPrivateKeyContentKey() {
        return privateKeyContentKey;
    }

    public String getPublicKeyContentKey() {
        return publicKeyContentKey;
    }

    public String getDomainKey() {
        return domainKey;
    }

    public int getPort() {
        return port;
    }

    public boolean useIpV6() {
        return useIpV6;
    }

    public StorageLayer.ProtectionEnable getProtectionDomainEnable() {
        return protectionDomainEnable;
    }

    public StorageLayer.ProtectionMode getProtectionDomainMode() {
        return protectionDomainMode;
    }

    public StorageLayer.ProtectionEnable getProtectionEntryEnable() {
        return protectionEntryEnable;
    }

    public StorageLayer.ProtectionMode getProtectionEntryMode() {
        return protectionEntryMode;
    }
}

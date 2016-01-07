package org.rmatil.sync.network.config;

import net.tomp2p.dht.StorageLayer;

public enum Config {

    DEFAULT("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", "SALT", 4001, false, 1024, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv6("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", "SALT", 4002, false, 1024, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv4("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", "SALT", 4003, false, 1024, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv4_2("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", "SALT", 4004, false, 1024, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER),
    IPv4_3("LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", "SALT", 4005, false, 1024, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER, StorageLayer.ProtectionEnable.ALL, StorageLayer.ProtectionMode.NO_MASTER);

    private String locationsContentKey;

    private String privateKeyContentKey;

    private String publicKeyContentKey;

    private String saltContentKey;

    private String domainKey;

    private int port;

    private boolean useIpV6;

    /**
     * Chunk size in bytes
     */
    private int chunkSize;

    private StorageLayer.ProtectionEnable protectionDomainEnable;
    private StorageLayer.ProtectionMode   protectionDomainMode;
    private StorageLayer.ProtectionEnable protectionEntryEnable;
    private StorageLayer.ProtectionMode   protectionEntryMode;

    /**
     * @param locationsLocationKey   The location key used for ClientLocations
     * @param privateKeyContentKey   The content key used for the private key
     * @param publicKeyContentKey    The content key used for the public key
     * @param saltContentKey         The content key used for the salt of the user
     * @param domainKey              The domain key
     * @param port                   The port of the client to use
     * @param useIpV6                Whether the client should use IPv6 addresses
     * @param chunkSize              Chunk size in bytes for file exchange
     * @param protectionDomainEnable Mode for domain protection
     * @param protectionDomainMode   Mode for domain protection
     * @param protectionEntryEnable  Mode for domain protection
     * @param protectionEntryMode    Mode for domain protection
     */
    Config(String locationsLocationKey,
           String privateKeyContentKey,
           String publicKeyContentKey,
           String saltContentKey,
           String domainKey,
           int port,
           boolean useIpV6,
           int chunkSize,
           StorageLayer.ProtectionEnable protectionDomainEnable,
           StorageLayer.ProtectionMode protectionDomainMode,
           StorageLayer.ProtectionEnable protectionEntryEnable,
           StorageLayer.ProtectionMode protectionEntryMode
    ) {
        this.locationsContentKey = locationsLocationKey;
        this.privateKeyContentKey = privateKeyContentKey;
        this.publicKeyContentKey = publicKeyContentKey;
        this.saltContentKey = saltContentKey;
        this.domainKey = domainKey;
        this.port = port;
        this.useIpV6 = useIpV6;
        this.chunkSize = chunkSize;
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

    public String getSaltContentKey() {
        return saltContentKey;
    }

    public String getDomainKey() {
        return domainKey;
    }

    /**
     * Allows to set the port manually
     *
     * @param port The port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public boolean useIpV6() {
        return useIpV6;
    }

    public long getChunkSize() {
        return chunkSize;
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

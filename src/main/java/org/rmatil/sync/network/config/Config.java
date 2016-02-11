package org.rmatil.sync.network.config;

public enum Config {

    DEFAULT("IDENTIFIER", "LOCATION", "PRIVATE_KEY", "PUBLIC_KEY", "DOMAIN KEY", "SALT", 1024);

    private String identifierContentKey;

    private String locationsContentKey;

    private String privateKeyContentKey;

    private String publicKeyContentKey;

    private String saltContentKey;

    private String domainKey;

    /**
     * Chunk size in bytes
     */
    private int chunkSize;

    /**
     * @param identifierContentKey The location key used for identifier
     * @param locationsLocationKey The location key used for ClientLocations
     * @param privateKeyContentKey The content key used for the private key
     * @param publicKeyContentKey  The content key used for the public key
     * @param saltContentKey       The content key used for the salt of the user
     * @param domainKey            The domain key
     * @param chunkSize            Chunk size in bytes for file exchange
     */
    Config(String identifierContentKey,
           String locationsLocationKey,
           String privateKeyContentKey,
           String publicKeyContentKey,
           String saltContentKey,
           String domainKey,
           int chunkSize
    ) {
        this.identifierContentKey = identifierContentKey;
        this.locationsContentKey = locationsLocationKey;
        this.privateKeyContentKey = privateKeyContentKey;
        this.publicKeyContentKey = publicKeyContentKey;
        this.saltContentKey = saltContentKey;
        this.domainKey = domainKey;
        this.chunkSize = chunkSize;
    }


    public String getIdentifierContentKey() {
        return identifierContentKey;
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

    public long getChunkSize() {
        return chunkSize;
    }
}

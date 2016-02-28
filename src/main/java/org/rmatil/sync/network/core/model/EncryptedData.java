package org.rmatil.sync.network.core.model;

import java.io.Serializable;

/**
 * Holds encrypted data for transmitting over the network
 */
public class EncryptedData implements Serializable {

    /**
     * Holds the encrypted symmetric key,
     * i.e. an RSA-encrypted AES key
     */
    protected byte[] encryptedKey;

    /**
     * Holds the symmetric encrypted data
     */
    protected byte[] encryptedData;

    /**
     * @param encryptedKey  The RSA encrypted symmetric key
     * @param encryptedData The symmetrically encrypted data
     */
    public EncryptedData(byte[] encryptedKey, byte[] encryptedData) {
        this.encryptedKey = encryptedKey;
        this.encryptedData = encryptedData;
    }

    /**
     * Returns the RSA encrypted symmetric key
     *
     * @return The encrypted symmetric key
     */
    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    /**
     * Returns the symmetrically encrypted data
     *
     * @return The encrypted data
     */
    public byte[] getEncryptedData() {
        return encryptedData;
    }
}

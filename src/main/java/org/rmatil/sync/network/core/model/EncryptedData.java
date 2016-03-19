package org.rmatil.sync.network.core.model;

import java.io.Serializable;

/**
 * Holds encrypted data for transmitting over the network
 */
public class EncryptedData implements Serializable {

    /**
     * The signature of the data
     */
    protected byte[] signature;

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
     * @param signature     The signature
     * @param encryptedKey  The RSA encrypted symmetric key
     * @param encryptedData The symmetrically encrypted data
     */
    public EncryptedData(byte[] signature, byte[] encryptedKey, byte[] encryptedData) {
        this.signature = signature;
        this.encryptedKey = encryptedKey;
        this.encryptedData = encryptedData;
    }

    /**
     * Returns the signature of the plain text
     * @return The signature of the plain text message
     */
    public byte[] getSignature() {
        return signature;
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

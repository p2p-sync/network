package org.rmatil.sync.network.core.security.encryption.symmetric;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.encryption.EncryptionMode;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

/**
 * This abstract class provides an interface to various
 * symmetric encryption implementations.
 */
public abstract class ASymmetricEncryption implements ISymmetricEncryption {

    /**
     * {@inheritDoc}
     */
    public byte[] encrypt(SecretKey symmetricKey, byte[] data)
            throws SecurityException {

        try {
            return process(EncryptionMode.ENCRYPT, symmetricKey, data);
        } catch (GeneralSecurityException | InvalidCipherTextException e) {
            throw new SecurityException("Failed to encrypt data. Message: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[] decrypt(SecretKey symmetricKey, byte[] data)
            throws SecurityException {

        try {
            return process(EncryptionMode.DECRYPT, symmetricKey, data);
        } catch (GeneralSecurityException | InvalidCipherTextException e) {
            throw new SecurityException("Failed to decrypt data. Message: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt or decrypt the given data.
     * <p>
     * Note, that the initialization vector which has been used gets prepended to the returned encrypted data.
     * These N bits have to been prepended too, if the data should be successfully decrypted.
     *
     * @param encryptionMode The mode of encryption: Either encrypt or decrypt
     * @param symmetricKey   The symmetric which should be used for encryption resp. decryption
     * @param data           The data to encrypt resp. decrypt
     *
     * @return The decrypted or encrypted data
     *
     * @throws InvalidCipherTextException If the cipher text was invalid
     * @throws GeneralSecurityException If another error occurred
     */
    protected abstract byte[] process(EncryptionMode encryptionMode, SecretKey symmetricKey, byte[] data)
            throws InvalidCipherTextException, GeneralSecurityException;
}

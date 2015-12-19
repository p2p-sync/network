package org.rmatil.sync.network.core.security.encryption.symmetric;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.encryption.EncryptionMode;

import javax.crypto.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

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
        } catch (IOException | InvalidCipherTextException e) {
            throw new SecurityException("Failed to encrypt data. Message: " + e.getMessage(), e);
        } catch (ShortBufferException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | NoSuchProviderException e) {
            throw new SecurityException("Failed to decrypt data. Message: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public byte[] decrypt(SecretKey symmetricKey, byte[] data)
            throws SecurityException {

        try {
            return process(EncryptionMode.DECRYPT, symmetricKey, data);
        } catch (IOException | InvalidCipherTextException e) {
            throw new SecurityException("Failed to decrypt data. Message: " + e.getMessage(), e);
        } catch (ShortBufferException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | NoSuchPaddingException | InvalidAlgorithmParameterException | IllegalBlockSizeException | NoSuchProviderException e) {
            throw new SecurityException("Failed to decrypt data. Message: " + e.getMessage());
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
     * @throws IOException                If accessing the data failed
     * @throws InvalidCipherTextException If the cipher text was invalid
     */
    protected abstract byte[] process(EncryptionMode encryptionMode, SecretKey symmetricKey, byte[] data)
            throws IOException, InvalidCipherTextException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, ShortBufferException, NoSuchProviderException, InvalidAlgorithmParameterException;
}

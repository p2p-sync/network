package org.rmatil.sync.network.core.security.encryption.symmetric;

import org.rmatil.sync.network.core.exception.SecurityException;

import javax.crypto.SecretKey;

/**
 * Specifies the interface for symmetric encryption resp. decryption of data.
 */
public interface ISymmetricEncryption {

    /**
     * Encrypts the given data with the given symmetric key.
     *
     * @param symmetricKey The symmetric key which has to been used for encryption
     * @param data         The data to symmetrically encrypt
     *
     * @return The encrypted data. Note, that the initialization vector has been prepended to the encrypted data, if necessary
     *
     * @throws SecurityException If encrypting the data failed
     */
    byte[] encrypt(SecretKey symmetricKey, byte[] data)
            throws SecurityException;

    /**
     * Encrypts the given data with the given symmetric key.
     *
     * @param symmetricKey The symmetric key which has to been used for encryption
     * @param initVector   The initialisation vector to use for CBC
     * @param data         The data to symmetrically encrypt
     *
     * @return The encrypted data. Note, that the initialization vector has been prepended to the encrypted data, if necessary
     *
     * @throws SecurityException If encrypting the data failed
     */
    byte[] encrypt(SecretKey symmetricKey, byte[] initVector, byte[] data)
            throws SecurityException;

    /**
     * Decrypts the given data with the specified symmetric key.
     * <p>
     * Note, that if an initialization vector has been used while encrypting, these bits
     * have to prepended to the given data.
     *
     * @param symmetricKey The symmetric key to use for decrypting the data
     * @param data         The data to decrypt (incl. the initialization vector, if necessary)
     *
     * @return The decrypted data
     *
     * @throws SecurityException If decrypting the data failed
     */
    byte[] decrypt(SecretKey symmetricKey, byte[] data)
            throws SecurityException;

    /**
     * Decrypts the given data with the specified symmetric key.
     *
     * @param symmetricKey The symmetric key to use for decrypting the data
     * @param initVector   The initialisation vector to use for decrypting
     * @param data         The data to decrypt (incl. the initialization vector, if necessary)
     *
     * @return The decrypted data
     *
     * @throws SecurityException If decrypting the data failed
     */
    byte[] decrypt(SecretKey symmetricKey, byte[] initVector, byte[] data)
            throws SecurityException;
}

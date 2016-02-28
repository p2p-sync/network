package org.rmatil.sync.network.core.security.encryption.asymmetric.rsa;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.rmatil.sync.network.core.exception.SecurityException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RsaEncryption {

    public RsaEncryption() {
        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Encrypts the given data with the specified RSA public key
     *
     * @param publicKey The public key to use for encrypting
     * @param data      The data to encrypt
     *
     * @return The encrypted data
     *
     * @throws SecurityException If encrypting the data fails
     */
    public byte[] encrypt(RSAPublicKey publicKey, byte[] data)
            throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return cipher.doFinal(data);
        } catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new SecurityException(e);
        }
    }

    /**
     * Decrypts the given data with the specified RSA private key
     *
     * @param privateKey The private key to use for decrypting
     * @param data       The data to decrypt
     *
     * @return The decrypted data
     *
     * @throws SecurityException If decrypting fails
     */
    public byte[] decrypt(RSAPrivateKey privateKey, byte[] data)
            throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(data);
        } catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new SecurityException(e);
        }
    }
}

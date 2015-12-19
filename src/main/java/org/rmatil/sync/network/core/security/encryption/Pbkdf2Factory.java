package org.rmatil.sync.network.core.security.encryption;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.SaltFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * Creates a key from a particular password using the PBKDF2WithHmacSHA1 key derivative function.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2898#section-5.2">RFC-2898: PBKDF2</a>
 */
public abstract class Pbkdf2Factory {

    protected static final String KEY_DERIVATIVE_FUNCTION = "PBKDF2WithHmacSHA1";
    protected static final String SECURITY_PROVIDER       = BouncyCastleProvider.PROVIDER_NAME;

    protected static final int SALT_LENGTH     = 16;
    protected static final int HASH_ITERATIONS = 10000;
    protected static final int KEY_LENGTH_256  = 256;
    protected static final int KEY_LENGTH_128  = 128;

    /**
     * Generates a secret key of the given password.
     * <p>
     * Note, that if using this function multiple times for the same password,
     * different outcomes are expected, since a salt is used to generate the password.
     *
     * @param password The password of which a secret key should be generated
     *
     * @return The generated secret key
     *
     * @throws SecurityException If generating the key failed
     */
    public static SecretKey generateKey(String password)
            throws SecurityException {

        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }

        SecretKeyFactory factory;
        byte[] salt;

        try {
            factory = SecretKeyFactory.getInstance(KEY_DERIVATIVE_FUNCTION, SECURITY_PROVIDER);
            salt = SaltFactory.generateSalt(SALT_LENGTH);

            // check whether UCE is enabled (Unrestricted Cryptography Extension)
            KeySpec keySpec;
            boolean uceEnabled = Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE;
            if (uceEnabled) {
                // we can use 256 bit keys
                keySpec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH_256);
            } else {
                // only 128 bit keys are allowed
                keySpec = new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH_128);
            }

            return factory.generateSecret(keySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new SecurityException("Failed to generate secret key. Message: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a secret key with the given password and salt
     *
     * @param password The password to use
     * @param salt     The salt to use
     *
     * @return Returns the created secret key
     */
    public static SecretKey generateKey(String password, String salt) {
        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }

        SecretKeyFactory factory;

        try {
            factory = SecretKeyFactory.getInstance(KEY_DERIVATIVE_FUNCTION, SECURITY_PROVIDER);

            // check whether UCE is enabled (Unrestricted Cryptography Extension)
            KeySpec keySpec;
            boolean uceEnabled = Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE;
            if (uceEnabled) {
                // we can use 256 bit keys
                keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), HASH_ITERATIONS, KEY_LENGTH_256);
            } else {
                // only 128 bit keys are allowed
                keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), HASH_ITERATIONS, KEY_LENGTH_128);
            }

            return factory.generateSecret(keySpec);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new SecurityException("Failed to generate secret key. Message: " + e.getMessage(), e);
        }
    }

}

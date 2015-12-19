package org.rmatil.sync.network.core.security;

import java.security.SecureRandom;

/**
 * Creates securely random salts
 */
public abstract class SaltFactory {

    private static final SecureRandom secRnd = new SecureRandom();

    /**
     * Generates a secure randomly salt of the given length
     *
     * @param length The length of the salt in bytes
     *
     * @return The salt of the specified length
     */
    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        secRnd.nextBytes(salt);

        return salt;
    }

}

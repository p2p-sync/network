package org.rmatil.sync.network.test.core.security.encryption;

import org.junit.Test;
import org.rmatil.sync.network.core.security.encryption.Pbkdf2Factory;

import javax.crypto.SecretKey;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class Pbkdf2FactoryTest {

    protected static final String PASSWORD = "ThisIsSafeDoNotChange";
    protected static final String SALT = "VerySecureSalt";

    @Test
    public void testGenerateKey() {
        SecretKey key1 = Pbkdf2Factory.generateKey(PASSWORD);
        SecretKey key2 = Pbkdf2Factory.generateKey(PASSWORD);

        assertThat("Key1 and key2 should be different since they use a different salt", key1.getEncoded(), not(equalTo(key2.getEncoded())));
    }

    @Test
    public void testGenerateKeyWithSalt() {
        SecretKey key1 = Pbkdf2Factory.generateKey(PASSWORD, SALT);
        SecretKey key2 = Pbkdf2Factory.generateKey(PASSWORD, SALT);

        assertArrayEquals("Key1 and key2 should be equal since they use the same salt", key1.getEncoded(), key2.getEncoded());
    }

    @Test
    public void testExceptions() {

    }
}

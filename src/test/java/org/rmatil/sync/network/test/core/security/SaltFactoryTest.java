package org.rmatil.sync.network.test.core.security;

import org.junit.Test;
import org.rmatil.sync.network.core.security.SaltFactory;

import static org.junit.Assert.*;

public class SaltFactoryTest {

    protected static final int SALT_LENGTH_16 = 16;
    protected static final int SALT_LENGTH_32 = 32;

    @Test
    public void testGenerateSalt() {
        byte[] salt16 = SaltFactory.generateSalt(SALT_LENGTH_16);
        byte[] salt32 = SaltFactory.generateSalt(SALT_LENGTH_32);

        assertEquals("Salt 16 was not 16 bytes long", SALT_LENGTH_16, salt16.length);
        assertEquals("Salt 32 was not 32 bytes long", SALT_LENGTH_32, salt32.length);
    }
}

package org.rmatil.sync.network.test.core.security.encryption.asymmetric.rsa;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.encryption.asymmetric.rsa.RsaEncryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class RsaEncryptionTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static RSAPublicKey  publicKey;
    protected static RSAPrivateKey privateKey;

    protected static RSAPublicKey  publicKey2;
    protected static RSAPrivateKey privateKey2;

    protected static RsaEncryption rsaEncryption;

    protected static final String DATA = "My Data with Umlauts: äüä";

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException {
        rsaEncryption = new RsaEncryption();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.genKeyPair();

        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        KeyPair keyPair2 = keyPairGenerator.genKeyPair();

        privateKey2 = (RSAPrivateKey) keyPair2.getPrivate();
        publicKey2 = (RSAPublicKey) keyPair2.getPublic();
    }

    @Test
    public void test() {
        byte[] encrypted = rsaEncryption.encrypt(publicKey, DATA.getBytes());

        assertThat("Encrypted byte sequence should not match plain byte sequence", encrypted, not(equalTo(DATA.getBytes())));

        byte[] decrypted = rsaEncryption.decrypt(privateKey, encrypted);

        assertArrayEquals("Decrypted data should be equal", decrypted, DATA.getBytes());
    }

    @Test
    public void testInvalidPrivateKey() {
        byte[] encrypted = rsaEncryption.encrypt(publicKey, DATA.getBytes());

        assertThat("Encrypted byte sequence should not match plain byte sequence", encrypted, not(equalTo(DATA.getBytes())));

        thrown.expect(SecurityException.class);
        byte[] decrypted = rsaEncryption.decrypt(privateKey2, encrypted);
    }
}

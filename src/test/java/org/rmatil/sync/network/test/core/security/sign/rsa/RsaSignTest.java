package org.rmatil.sync.network.test.core.security.sign.rsa;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.core.security.sign.rsa.RsaSign;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.Assert.assertTrue;

public class RsaSignTest {

    private static RsaSign rsaSign = new RsaSign();
    private static byte[]  data    = new byte[1024];

    private static RSAPublicKey  publicKey;
    private static RSAPrivateKey privateKey;

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

        KeyPair keyPair = keyGen.genKeyPair();

        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Test
    public void test() {
        byte[] signature = rsaSign.sign(privateKey, data);

        boolean isVerified = rsaSign.verify(publicKey, signature, data);

        assertTrue("Signature should be valid", isVerified);
    }
}

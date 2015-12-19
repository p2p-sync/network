package org.rmatil.sync.network.test.core.security.encryption.symmetric.aes;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.encryption.symmetric.ASymmetricEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesEncryption;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import static org.junit.Assert.*;

public class AesEncryptTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static ASymmetricEncryption aesEncrypt;

    protected static SecretKey secretKey256;

    protected static SecretKey secretKey128;

    protected static final String DATA = "My Data with Umlauts: äüö";

    @BeforeClass
    public static void setUp()
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        aesEncrypt = new AesEncryption();
        // https://tools.ietf.org/html/rfc2898#section-5.2
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "BC");

        // 128 bit
        byte[] salt = new byte[16];
        SecureRandom rnd = new SecureRandom();
        rnd.nextBytes(salt);

        // pbe := password based encryption
        KeySpec keySpec256 = new PBEKeySpec("password".toCharArray(), salt, 1000, 256);
        secretKey256 = factory.generateSecret(keySpec256);

        KeySpec keySpec128 = new PBEKeySpec("password".toCharArray(), salt, 1000, 128);
        secretKey128 = factory.generateSecret(keySpec128);
    }

    @Test
    public void testEncryptWeak()
            throws NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidCipherTextException {

        byte[] encrypted128 = aesEncrypt.encrypt(secretKey128, DATA.getBytes(StandardCharsets.UTF_8));
        byte[] decrypted128 = aesEncrypt.decrypt(secretKey128, encrypted128);

        String ret128 = new String(decrypted128, StandardCharsets.UTF_8);
        assertEquals("String is not correctly en-/decrypted using the 128 bit key", DATA, ret128);
    }

    @Test
    public void testEncryptStrong()
            throws NoSuchAlgorithmException {
        if (Cipher.getMaxAllowedKeyLength("AES") != Integer.MAX_VALUE) {
            // UCE is not enabled -> then we except that exception is thrown
            thrown.expect(SecurityException.class);
        }

        byte[] encrypted = aesEncrypt.encrypt(secretKey256, DATA.getBytes(StandardCharsets.UTF_8));
        byte[] decrypted = aesEncrypt.decrypt(secretKey256, encrypted);

        String ret = new String(decrypted, StandardCharsets.UTF_8);
        assertEquals("String is not correctly en-/decrypted using the 256 bit key", DATA, ret);
    }

}

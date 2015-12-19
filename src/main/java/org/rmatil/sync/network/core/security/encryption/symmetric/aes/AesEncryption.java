package org.rmatil.sync.network.core.security.encryption.symmetric.aes;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.EncryptionMode;
import org.rmatil.sync.network.core.security.encryption.symmetric.ASymmetricEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;

/**
 * Encrypts or decrypts data using the AES (Advanced Encryption Standard) algorithm.
 */
public final class AesEncryption extends ASymmetricEncryption {

    protected final static Logger logger = LoggerFactory.getLogger(AesEncryption.class);

    /**
     * The length of the initialization vector for Cipher Block Chaining (CBC) in bits
     */
    public static final int INIT_VECTOR_LENGTH = 16;

    /**
     * Generates a randomly (and most probably unique) initialization vector
     * which can be used for Cipher Block Chaining (CBC).
     *
     * @see AesEncryption#INIT_VECTOR_LENGTH The length of the initialization vector
     *
     * @return The initialization vector for CBC
     */
    public static byte[] generateInitializationVector() {
        SecureRandom secRnd = new SecureRandom();
        byte[] initVector = new byte[INIT_VECTOR_LENGTH];

        secRnd.nextBytes(initVector);

        return initVector;
    }

    /**
     * Initializes a new AES encryption utility. Adds
     * Bouncy Castle as security provider if not set yet.
     */
    public AesEncryption() {
        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    protected byte[] process(EncryptionMode encryptionMode, SecretKey symmetricKey, byte[] data)
            throws IOException, InvalidCipherTextException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidKeyException, IllegalBlockSizeException, ShortBufferException, NoSuchProviderException, InvalidAlgorithmParameterException {

        boolean isEncrypting = false;
        if (EncryptionMode.ENCRYPT == encryptionMode) {
            isEncrypting = true;
        }

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey.getEncoded(), "AES");

        byte[] initVector;
        if (EncryptionMode.ENCRYPT == encryptionMode) {
             initVector = generateInitializationVector();
        } else {
            // apparently, there is no need to keep the iv secret
            // init vector is prepended to data
            initVector = new byte[INIT_VECTOR_LENGTH];
            byte[] tmpData = new byte[data.length - INIT_VECTOR_LENGTH];
            System.arraycopy(data, 0, initVector, 0, INIT_VECTOR_LENGTH);
            System.arraycopy(data, INIT_VECTOR_LENGTH, tmpData, 0, data.length - INIT_VECTOR_LENGTH);
            // copy data back without init vector in it
            data = tmpData;
        }

        byte[] processedData;

        // getMaxAllowedKeyLength returns max int value if Unrestricted Cryptography Extenstion is enabled
        boolean uceIsEnabled = Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE;

        // in bits
        int keySize = symmetricKey.getEncoded().length * 8;
        if (! uceIsEnabled) {

            if (keySize > 128) {
                throw new SecurityException("Max. allowed keysize is 128 bit if UCE (Unrestricted Cryptography Extension) is not enabled");
            }

            logger.info("Using weak AES encryption. Key size was: " + keySize + " bits and max allowed key length was: " + Cipher.getMaxAllowedKeyLength("AES"));
            processedData = this.processWeak(isEncrypting, keySpec, initVector, data);
        } else {
            logger.info("Using strong AES encryption. Key size was: " + keySize + " bits and max allowed key length was: " + Cipher.getMaxAllowedKeyLength("AES"));
            processedData = this.processUce(isEncrypting, keySpec, initVector, data);
        }

        // Prepend the initialization vector to the data
        // Not required to be encrypted: http://security.stackexchange.com/questions/17044/when-using-aes-and-cbc-is-it-necessary-to-keep-the-iv-secret
        if (EncryptionMode.ENCRYPT == encryptionMode) {
            byte[] dataWithInitVector = new byte[processedData.length + INIT_VECTOR_LENGTH];
            System.arraycopy(initVector, 0, dataWithInitVector, 0, INIT_VECTOR_LENGTH);
            System.arraycopy(processedData, 0, dataWithInitVector, INIT_VECTOR_LENGTH, processedData.length);

            return dataWithInitVector;
        }

        return processedData;
    }

    /**
     * Encrypt using UCE (Unlimited Cryptography Extension), i.e. for keys with length greater than 128 bit
     *
     * @return The processed data
     *
     * @throws InvalidCipherTextException If padding is expected and not found
     */
    protected byte[] processUce(boolean isEncrypting, SecretKeySpec symmetricKeySpec, byte[] initVector, byte[] data)
            throws InvalidCipherTextException {
        AESEngine aesEngine = new AESEngine();
        // CBC: Cipher Block Chaining
        // -> Chain each cipher block to the predecessor to prevent attacks on exchanging single blocks
        // -> requires an initialization vector on the first block
        // -> IV must be random and unique
        CBCBlockCipher cbc = new CBCBlockCipher(aesEngine);
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbc);

        CipherParameters initVectorAndKey = new ParametersWithIV(new KeyParameter(symmetricKeySpec.getEncoded()), initVector);
        cipher.init(isEncrypting, initVectorAndKey);

        byte[] output = new byte[cipher.getOutputSize(data.length)];
        int bytesProcessed1 = cipher.processBytes(data, 0, data.length, output, 0);
        // flush the last block, even if not full
        int bytesProcessed2 = cipher.doFinal(output, bytesProcessed1);

        byte[] processedData = new byte[bytesProcessed1 + bytesProcessed2];
        System.arraycopy(output, 0, processedData, 0, processedData.length);

        return processedData;
    }

    /**
     * Process data with keys of maximum 128 bit length
     *
     * @return The processed data
     */
    protected byte[] processWeak(boolean isEncrypting, SecretKeySpec symmetricKeySpec, byte[] initVector, byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, ShortBufferException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initVector);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", BouncyCastleProvider.PROVIDER_NAME);

        int encryptMode = isEncrypting ? Cipher.ENCRYPT_MODE :  Cipher.DECRYPT_MODE;
        cipher.init(encryptMode, symmetricKeySpec, ivParameterSpec);

        byte[] output = new byte[cipher.getOutputSize(data.length)];
        int bytesProcessed1 = cipher.update(data, 0, data.length, output, 0);
        // flush the last block, even if not full
        int bytesProcessed2 = cipher.doFinal(output, bytesProcessed1);

        byte[] processedData = new byte[bytesProcessed1 + bytesProcessed2];
        System.arraycopy(output, 0, processedData, 0, processedData.length);

        return processedData;
    }
}

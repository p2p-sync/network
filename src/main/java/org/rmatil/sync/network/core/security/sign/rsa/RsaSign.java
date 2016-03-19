package org.rmatil.sync.network.core.security.sign.rsa;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.security.sign.ISign;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RsaSign implements ISign<RSAPublicKey, RSAPrivateKey> {

    public final static String SIGNATURE_ALGORITHM = "SHA1withRSA";

    public RsaSign() {
        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public byte[] sign(RSAPrivateKey privateKey, byte[] data)
            throws SecurityException {

        try {
            Signature signEngine = Signature.getInstance(RsaSign.SIGNATURE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            signEngine.initSign(privateKey);
            signEngine.update(data);
            return signEngine.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            throw new SecurityException(e);
        }
    }

    @Override
    public boolean verify(RSAPublicKey publicKey, byte[] signature, byte[] data)
            throws SecurityException {

        try {
            Signature signEngine = Signature.getInstance(RsaSign.SIGNATURE_ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
            signEngine.initVerify(publicKey);
            signEngine.update(data);
            return signEngine.verify(signature);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            throw new SecurityException(e);
        }
    }
}

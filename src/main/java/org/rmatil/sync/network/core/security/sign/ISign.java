package org.rmatil.sync.network.core.security.sign;

import org.rmatil.sync.network.core.exception.SecurityException;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface ISign<P extends PublicKey, K extends PrivateKey> {

    byte[] sign(K privateKey, byte[] data)
            throws SecurityException;

    boolean verify(P publicKey, byte[] signature, byte[] data)
            throws SecurityException;
}

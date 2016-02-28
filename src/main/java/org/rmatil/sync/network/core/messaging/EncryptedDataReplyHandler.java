package org.rmatil.sync.network.core.messaging;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.core.exception.ObjectSendFailedException;
import org.rmatil.sync.network.core.exception.SecurityException;
import org.rmatil.sync.network.core.model.EncryptedData;
import org.rmatil.sync.network.core.security.encryption.asymmetric.rsa.RsaEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesEncryption;
import org.rmatil.sync.network.core.security.encryption.symmetric.aes.AesKeyFactory;
import org.rmatil.sync.network.core.serialize.ByteSerializer;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Decrypts incoming data and passes the plain data
 * to the object data reply handler specified on creation of
 * this object.
 * Furthermore, if the object data reply handler returns data directly,
 * it will be encrypted and then sent back to the originating client.
 */
public class EncryptedDataReplyHandler implements ObjectDataReply {

    protected ObjectDataReplyHandler objectDataReplyHandler;

    protected INodeManager nodeManager;

    protected RSAPrivateKey privateKey;

    protected RsaEncryption rsaEncryption;
    protected AesEncryption aesEncryption;

    /**
     * @param objectDataReplyHandler The object data reply handler to which the decrypted data should be passed
     * @param nodeManager            The node manager to fetch the public key of the receiver to encrypt the data
     * @param rsaPrivateKey          The private key of the user to decrypt incoming data
     */
    public EncryptedDataReplyHandler(ObjectDataReplyHandler objectDataReplyHandler, INodeManager nodeManager, RSAPrivateKey rsaPrivateKey) {
        this.objectDataReplyHandler = objectDataReplyHandler;
        this.nodeManager = nodeManager;
        this.privateKey = rsaPrivateKey;
        this.rsaEncryption = new RsaEncryption();
        this.aesEncryption = new AesEncryption();
    }

    @Override
    public Object reply(PeerAddress sender, Object request)
            throws Exception {

        if (! (request instanceof EncryptedData)) {
            // pass the plain object
            return this.objectDataReplyHandler.reply(sender, request);
        }

        EncryptedData encryptedData = (EncryptedData) request;

        byte[] decryptedKey = this.rsaEncryption.decrypt(this.privateKey, encryptedData.getEncryptedKey());
        // init vector is prepended to data
        byte[] initVector = Arrays.copyOfRange(decryptedKey, 0, AesEncryption.INIT_VECTOR_LENGTH);
        byte[] encodedAesKey = Arrays.copyOfRange(decryptedKey, AesEncryption.INIT_VECTOR_LENGTH, decryptedKey.length);

        SecretKey aesKey = new SecretKeySpec(encodedAesKey, 0, encodedAesKey.length, "AES");

        byte[] decryptedData = this.aesEncryption.decrypt(aesKey, initVector, encryptedData.getEncryptedData());

        Object object = ByteSerializer.fromBytes(decryptedData);

        // -> invoke object data reply
        IResponse response = this.objectDataReplyHandler.reply(sender, object);

        // no need to encrypt something, if the direct response is null
        if (null == response) {
            return response;
        }

        // encrypt the returned data
        // get public key from receiver to encrypt
        RSAPublicKey publicKey;
        try {
            publicKey = (RSAPublicKey) this.nodeManager.getPublicKey(response.getReceiverAddress().getUsername());
        } catch (InputOutputException e) {
            throw new ObjectSendFailedException(
                    "Could not use public key of user "
                            + response.getReceiverAddress().getUsername()
                            + " to encrypt data. Aborting to send request for this receiver. Message: "
                            + e.getMessage()
            );
        }

        try {
            // encrypt the actual data using the AES key
            initVector = AesEncryption.generateInitializationVector();
            aesKey = AesKeyFactory.generateSecretKey();
            byte[] aesEncryptedData = this.aesEncryption.encrypt(aesKey, initVector, ByteSerializer.toBytes(response));

            // encrypt the AES key with RSA
            encodedAesKey = aesKey.getEncoded();
            byte[] symmetricKey = new byte[AesEncryption.INIT_VECTOR_LENGTH + encodedAesKey.length];

            System.arraycopy(initVector, 0, symmetricKey, 0, initVector.length);
            System.arraycopy(encodedAesKey, 0, symmetricKey, initVector.length, encodedAesKey.length);

            byte[] rsaEncryptedData = this.rsaEncryption.encrypt(publicKey, symmetricKey);

            return new EncryptedData(rsaEncryptedData, aesEncryptedData);
        } catch (IOException | SecurityException e) {
            throw new ObjectSendFailedException(
                    "Failed to encrypt data for receiver "
                            + response.getReceiverAddress().getUsername()
                            + " ("
                            + response.getReceiverAddress().getIpAddress()
                            + ":"
                            + response.getReceiverAddress().getPort()
                            + "). Aborting to send response for this receiver. Message: "
                            + e.getMessage()
            );
        }
    }
}

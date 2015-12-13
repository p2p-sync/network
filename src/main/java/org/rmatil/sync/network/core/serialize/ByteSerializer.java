package org.rmatil.sync.network.core.serialize;

import java.io.*;

/**
 * Serializes objects from resp. to a byte array
 */
public abstract class ByteSerializer {

    /**
     * Serializes the given object to an array ob bytes
     *
     * @param object The object to serialize
     * @return A byte array representation of the given object
     * @throws IOException If serializing failed
     */
    public static byte[] toBytes(Object object)
            throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            out.close();

            return bos.toByteArray();
        }
    }

    /**
     * Deserializes the given byte array into an object
     * @param bytes The byte array to deserialize
     * @return An object representation of the byte array
     *
     * @throws IOException If deserializing failed
     * @throws ClassNotFoundException If deserializing to Object failed
     */
    public static Object fromBytes(byte[] bytes)
            throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            Object object = in.readObject();
            in.close();

            return object;
        }
    }
}

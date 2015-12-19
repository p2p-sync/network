package org.rmatil.sync.network.core.exception;

/**
 * Thrown, if an error occurred during encrypting or decrypting data
 */
public class SecurityException extends RuntimeException {

    public SecurityException() {
        super();
    }

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException(Throwable cause) {
        super(cause);
    }
}

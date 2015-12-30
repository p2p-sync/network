package org.rmatil.sync.network.core.exception;

public class ConnectionFailedException extends RuntimeException {

    public ConnectionFailedException() {
        super();
    }

    public ConnectionFailedException(String message) {
        super(message);
    }

    public ConnectionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionFailedException(Throwable cause) {
        super(cause);
    }
}

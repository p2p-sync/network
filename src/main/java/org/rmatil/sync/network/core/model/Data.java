package org.rmatil.sync.network.core.model;

import java.io.Serializable;

/**
 * A wrapper holding the actual data of
 * an exchange request
 *
 * @see org.rmatil.sync.network.api.IRequest
 */
public class Data implements Serializable {

    /**
     * The byte content
     */
    protected byte[] content;

    /**
     * Whether the byte content is encrypted or not
     *
     * @see Data#content
     */
    protected boolean isEncrypted;

    /**
     * @param content     The actual content of the data package
     * @param isEncrypted Whether the data is encrypted
     */
    public Data(byte[] content, boolean isEncrypted) {
        this.content = content;
        this.isEncrypted = isEncrypted;
    }

    /**
     * Returns the content in bytes
     *
     * @return The actual content of this data package
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns true if the content of this package is encrypted.
     *
     * @return True if the content is encrypted, false otherwise
     */
    public boolean isEncrypted() {
        return isEncrypted;
    }
}

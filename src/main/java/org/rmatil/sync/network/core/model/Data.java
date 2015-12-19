package org.rmatil.sync.network.core.model;

public class Data {

    protected byte[] content;

    protected boolean isEncrypted;

    public Data(byte[] content, boolean isEncrypted) {
        this.content = content;
        this.isEncrypted = isEncrypted;
    }

    public byte[] getContent() {
        return content;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }
}

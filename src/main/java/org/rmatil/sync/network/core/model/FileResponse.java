package org.rmatil.sync.network.core.model;

import java.io.Serializable;

public class FileResponse implements Serializable {

    /**
     * The number of the chunk which is returned
     */
    protected long chunkCounter;

    /**
     * The total number of chunks which have
     * to been requested to get the complete file
     */
    protected long totalNrOfChunks;

    /**
     * The file size in bytes
     */
    protected long totalFileSize;

    /**
     * The actual data of the request
     */
    protected Data data;

    public FileResponse(long chunkCounter, long totalNrOfChunks, long totalFileSize, Data data) {
        this.chunkCounter = chunkCounter;
        this.totalNrOfChunks = totalNrOfChunks;
        this.totalFileSize = totalFileSize;
        this.data = data;
    }

    public long getChunkCounter() {
        return chunkCounter;
    }

    public long getTotalNrOfChunks() {
        return totalNrOfChunks;
    }

    public long getTotalFileSize() {
        return totalFileSize;
    }

    public Data getData() {
        return data;
    }
}

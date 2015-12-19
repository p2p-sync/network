package org.rmatil.sync.network.core.model;

public class FileResponse {

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
     * The actual data of the request
     */
    protected Data data;

    public FileResponse(long chunkCounter, long totalNrOfChunks, Data data) {
        this.chunkCounter = chunkCounter;
        this.totalNrOfChunks = totalNrOfChunks;
        this.data = data;
    }

    public long getChunkCounter() {
        return chunkCounter;
    }

    public long getTotalNrOfChunks() {
        return totalNrOfChunks;
    }

    public Data getData() {
        return data;
    }
}

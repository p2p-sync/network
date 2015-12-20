package org.rmatil.sync.network.core.model;

import org.rmatil.sync.network.api.IUser;

import java.io.Serializable;

public class FileRequest implements Serializable {

    /**
     * The requesting user
     */
    protected IUser user;

    /**
     * The relative path to the file which should be returned
     */
    protected String relativeFilePath;

    /**
     * The counter which indicates which chunk should be requested
     */
    protected long chunkCounter;

    /**
     * @param user             The requesting user
     * @param relativeFilePath The relative path to the file which should be returned
     */
    public FileRequest(IUser user, String relativeFilePath, long chunkCounter) {
        this.user = user;
        this.relativeFilePath = relativeFilePath;
        this.chunkCounter = chunkCounter;
    }

    /**
     * Returns the requesting user
     *
     * @return The requesting user
     */
    public IUser getUser() {
        return user;
    }

    /**
     * Returns the relative path to the file which should be returned
     *
     * @return The relative path
     */
    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    /**
     * Returns the chunk index of the chunk which should be returned
     *
     * @return The chunk number to return
     */
    public long getChunkCounter() {
        return chunkCounter;
    }
}

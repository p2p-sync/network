package org.rmatil.sync.network.api;

/**
 * Note, that any implementation of this callback <b>must</b> have a nullary constructor.
 * Otherwise, the dynamic instantiation of the request callback can not be guaranteed.
 * <p>
 * All dependencies of a callback are set through setter methods.
 */
public interface IRequestCallback extends Runnable {

    /**
     * Set the client to use for sending responses back
     *
     * @param client The client to use
     */
    void setClient(IClient client);

    /**
     * Set the request to handle
     *
     * @param request The request to handle
     */
    void setRequest(IRequest request);

}

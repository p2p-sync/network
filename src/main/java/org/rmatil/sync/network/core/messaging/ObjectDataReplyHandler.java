package org.rmatil.sync.network.core.messaging;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.rmatil.sync.network.api.*;
import org.rmatil.sync.network.core.ANetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this handler to register multiple implementations of ObjectDataReply
 * for specific classes which get invoked if the request object is matching the class.
 */
public class ObjectDataReplyHandler implements ObjectDataReply {

    protected final static Logger logger = LoggerFactory.getLogger(ObjectDataReplyHandler.class);

    /**
     * A map of all registered responseCallbackHandlers.
     * Key is the class to which the corresponding objectDataReply should be applied to
     */
    protected Map<UUID, IResponseCallback> responseCallbackHandlers;

    /**
     * A map of all registered requestCallbackHandlers.
     * Key is the class of the request for which the corresponding request callback should be invoked.
     * <p>
     * (Key is any implementation of {@link IRequest}, value any implementation of {@link IRequestCallback})
     */
    protected Map<Class<? extends IRequest>, Class<? extends IRequestCallback>> requestCallbackHandlers;

    /**
     * A map of all currently running request callback handlers along
     * with their start time
     */
    protected final Map<Long, UUID> runningRequestCallbacks;

    /**
     * The node to use for sending back responses
     */
    protected INode node;

    /**
     * @param node                     The node used for sending back the responses of a request
     * @param responseCallbackHandlers A map of all registered responseCallbackHandlers. Specify as key the exchange id of the request
     *                                 the corresponding objectDataReply should be applied to if the request matches the class
     * @param requestCallbackHandlers  A map of all registered requestCallbackHandlers. Specify as key the class of the request for which the corresponding request callback handler should be invoked
     */
    public ObjectDataReplyHandler(INode node, Map<UUID, IResponseCallback> responseCallbackHandlers, Map<Class<? extends IRequest>, Class<? extends IRequestCallback>> requestCallbackHandlers) {
        this.node = node;
        this.responseCallbackHandlers = responseCallbackHandlers;
        this.requestCallbackHandlers = requestCallbackHandlers;
        this.runningRequestCallbacks = new ConcurrentHashMap<>();
    }

    /**
     * @param node The node used for sending back the responses of a request
     */
    public ObjectDataReplyHandler(INode node) {
        this.node = node;
        this.responseCallbackHandlers = new HashMap<>();
        this.requestCallbackHandlers = new HashMap<>();
        this.runningRequestCallbacks = new ConcurrentHashMap<>();
    }

    /**
     * Adds an objectDataReply for the given class
     *
     * @param requestExchangeId The request exchange id to which the callback should be registered
     * @param responseCallback  The objectDataReply which should be applied if the request is instance of the specified class above
     */
    public void addResponseCallbackHandler(UUID requestExchangeId, IResponseCallback responseCallback) {
        this.responseCallbackHandlers.put(requestExchangeId, responseCallback);
    }

    /**
     * Removes the objectDataReply for the class with the given name (if present)
     *
     * @param requestExchangeId The request exchange id of which the callback handler should be removed
     */
    public void removeResponseCallbackHandler(UUID requestExchangeId) {
        this.responseCallbackHandlers.remove(requestExchangeId);
    }

    /**
     * Returns all registered responseCallbackHandlers
     *
     * @return All registered response callbacks
     */
    public Map<UUID, IResponseCallback> getResponseCallbackHandlers() {
        return this.responseCallbackHandlers;
    }

    /**
     * Add a request callback handler which will be called if an request
     * is incoming matching the given class
     *
     * @param clazz           The request class to register the callback (Any implementation of {@link IRequest})
     * @param requestCallback The request callback which should be called if the specified request gets to the node (Any implementation of {@link IRequestCallback})
     */
    public void addRequestCallbackHandler(Class<? extends IRequest> clazz, Class<? extends IRequestCallback> requestCallback) {
        this.requestCallbackHandlers.put(clazz, requestCallback);
    }

    /**
     * Remove the callback handler for the given request class
     *
     * @param clazz The request class for which to remove the callback handler
     */
    public void removeRequestCallbackHandler(Class<? extends IRequest> clazz) {
        this.requestCallbackHandlers.remove(clazz);
    }

    /**
     * Returns all registered request callback handlers
     *
     * @return All registered request callback handlers
     */
    public Map<Class<? extends IRequest>, Class<? extends IRequestCallback>> getRequestCallbackHandlers() {
        return this.requestCallbackHandlers;
    }

    /**
     * Returns true if request callback are running.
     * False otherwise.
     *
     * @return True, if other request callbacks are running, false otherwise
     */
    public boolean areRequestCallbacksRunning() {
        synchronized (this.runningRequestCallbacks) {
            Iterator<Map.Entry<Long, UUID>> itr = this.runningRequestCallbacks.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry<Long, UUID> entry = itr.next();
                // remove expired callbacks
                if (entry.getKey() < System.currentTimeMillis()) {
                    logger.trace("Callback with id " + entry.getValue() + " will likely not be running anymore. Started at " + entry.getKey() + ", now is " + System.currentTimeMillis());
                    itr.remove();
                } else {
                    logger.trace("Callback with id " + entry.getValue() + " is still running. Started at " + entry.getKey() + ", now is " + System.currentTimeMillis());
                }
            }

            return ! this.runningRequestCallbacks.isEmpty();
        }
    }

    @Override
    public IResponse reply(PeerAddress sender, Object request)
            throws Exception {

        // forward the request to the correct data reply instance
        if (request instanceof IRequest) {
            if (this.requestCallbackHandlers.containsKey(request.getClass())) {
                logger.debug("Using " + this.requestCallbackHandlers.get(request.getClass()).getName() + " as handler for request " + ((IRequest) request).getExchangeId());
                Class<? extends IRequestCallback> requestCallbackClass = this.requestCallbackHandlers.get(request.getClass());

                // create a new instance running in its own thread
                IRequestCallback requestCallback = requestCallbackClass.newInstance();
                requestCallback.setNode(this.node);

                requestCallback.setRequest((IRequest) request);

                Thread thread = new Thread(requestCallback);
                thread.setName("RequestCallback for request " + ((IRequest) request).getExchangeId());
                thread.start();

                this.runningRequestCallbacks.put(
                        System.currentTimeMillis() + ANetworkHandler.MAX_WAITING_TIME,
                        ((IRequest) request).getExchangeId()
                );


                return null;
            }
        }

        // if we receive a response, we forward it to the correct callback handler
        if (request instanceof IResponse) {
            if (this.responseCallbackHandlers.containsKey(((IResponse) request).getExchangeId())) {
                IResponseCallback responseCallback = this.responseCallbackHandlers.get(((IResponse) request).getExchangeId());
                logger.debug("Using " + responseCallback.getClass().getName() + " as handler for response " + ((IResponse) request).getExchangeId());

                responseCallback.onResponse((IResponse) request);

                return null;
            }
        }

        logger.warn("No appropriate object data reply instance found for request " + request.getClass().getName() + ". Sending NULL as response!");
        return null;
    }
}

package org.rmatil.sync.network.core.messaging;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IResponse;
import org.rmatil.sync.network.api.IResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Use this handler to register multiple implementations of ObjectDataReply
 * for specific classes which get invoked if the request object is matching the class.
 */
public class ObjectDataReplyHandler implements ObjectDataReply {

    protected final static Logger logger = LoggerFactory.getLogger(ObjectDataReplyHandler.class);

    /**
     * A map of all registered callbackHandlers.
     * Key is the class to which the corresponding objectDataReply should be applied to
     */
    protected Map<UUID, IResponseCallback> callbackHandlers;

    /**
     * The client to use for sending back responses
     */
    protected IClient client;

    /**
     * @param client           The client used for sending back the responses of a request
     * @param callbackHandlers A map of all registered callbackHandlers. Specify as key the exchange id of the request
     *                         the corresponding objectDataReply should be applied to if the request matches the class
     */
    public ObjectDataReplyHandler(IClient client, Map<UUID, IResponseCallback> callbackHandlers) {
        this.client = client;
        this.callbackHandlers = callbackHandlers;
    }

    public ObjectDataReplyHandler(IClient client) {
        this.client = client;
        this.callbackHandlers = new HashMap<>();
    }

    /**
     * Adds an objectDataReply for the given class
     *
     * @param requestExchangeId The request exchange id to which the callback should be registered
     * @param responseCallback  The objectDataReply which should be applied if the request is instance of the specified class above
     */
    public void addCallbackHandler(UUID requestExchangeId, IResponseCallback responseCallback) {
        this.callbackHandlers.put(requestExchangeId, responseCallback);
    }

    /**
     * Removes the objectDataReply for the class with the given name (if present)
     *
     * @param requestExchangeId The request exchange id of which the callback handler should be removed
     */
    public void removeObjectDataReply(UUID requestExchangeId) {
        this.callbackHandlers.remove(requestExchangeId);
    }

    /**
     * Returns all registered callbackHandlers
     *
     * @return All registered response callbacks
     */
    public Map<UUID, IResponseCallback> getCallbackHandlers() {
        return this.callbackHandlers;
    }

    @Override
    public Object reply(PeerAddress sender, Object request)
            throws Exception {

        // forward the request to the correct data reply instance
        if (request instanceof IRequest) {
            logger.info("Starting thread for request " + ((IRequest) request).getExchangeId());
            // Set the client so that the request can send back responses
            ((IRequest) request).setClient(this.client);
            // let the request be handled in its own thread
            new Thread((IRequest) request).start();

            return null;
        }

        // if we receive a response, we forward it to the correct callback handler
        if (request instanceof IResponse) {
            for (Map.Entry<UUID, IResponseCallback> entry : this.callbackHandlers.entrySet()) {
                if (entry.getKey().equals(((IResponse) request).getExchangeId())) {
                    logger.info("Using " + entry.getValue().getClass().getName() + " as handler for response " + entry.getKey().toString());
                    entry.getValue().onResponse((IResponse) request);
                    return null;
                }
            }
        }

        logger.warn("No appropriate object data reply instance found for request " + request.getClass().getName() + ". Sending NULL as response!");
        return null;
    }
}

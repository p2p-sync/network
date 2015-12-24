package org.rmatil.sync.network.core.messaging;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Use this handler to register multiple implementations of ObjectDataReply
 * for specific classes which get invoked if the request object is matching the class.
 */
public class ObjectDataReplyHandler implements ObjectDataReply {

    protected final static Logger logger = LoggerFactory.getLogger(ObjectDataReplyHandler.class);

    /**
     * A map of all registered objectDataReplies.
     * Key is the class to which the corresponding objectDataReply should be applied to
     */
    protected Map<Class, ObjectDataReply> objectDataReplies;

    /**
     * @param objectDataReplies A map of all registered objectDataReplies. Specify as key the class to which
     *                          the corresponding objectDataReply should be applied to if the request matches the class
     */
    public ObjectDataReplyHandler(Map<Class, ObjectDataReply> objectDataReplies) {
        this.objectDataReplies = objectDataReplies;
    }

    public ObjectDataReplyHandler() {
        this.objectDataReplies = new HashMap<>();
    }

    /**
     * Adds an objectDataReply for the given class
     *
     * @param clazz           The class to which the objectDataReply should be applied to
     * @param objectDataReply The objectDataReply which should be applied if the request is instance of the specified class above
     */
    public void addObjectDataReply(Class clazz, ObjectDataReply objectDataReply) {
        this.objectDataReplies.put(clazz, objectDataReply);
    }

    /**
     * Removes the objectDataReply for the class with the given name (if present)
     *
     * @param clazz The class of which the objectDataReply should be removed
     */
    public void removeObjectDataReply(Class clazz) {
        this.objectDataReplies.remove(clazz);
    }

    /**
     * Returns all registered objectDataReplies
     *
     * @return All registered ObjectDataReplies
     */
    public Map<Class, ObjectDataReply> getObjectDataReplies() {
        return this.objectDataReplies;
    }

    @Override
    public Object reply(PeerAddress sender, Object request)
            throws Exception {

        // forward the request to the correct data reply instance
        for (Map.Entry<Class, ObjectDataReply> entry : this.objectDataReplies.entrySet()) {
            if (entry.getKey().isInstance(request)) {
                logger.info("Using " + entry.getKey().getName() + " as handler for request");
                return entry.getValue().reply(sender, request);
            }
        }

        // TODO: check if null is an appropriate response. Maybe an empty object should be returned?
        logger.warn("No appropriate object data reply instance found for request. Sending NULL as response!");
        return null;
    }
}

package org.rmatil.sync.network.test.core.messaging;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IRequestCallback;
import org.rmatil.sync.network.api.IResponseCallback;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.test.core.DummyNetworkHandler;
import org.rmatil.sync.network.test.core.DummyRequest;
import org.rmatil.sync.network.test.core.DummyRequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ObjectDataReplyHandlerTest {

    protected static ObjectDataReplyHandler objectDataReplyHandler;
    protected static IResponseCallback      responseCallback;
    protected static UUID                                                              responseCallbackUUID     = UUID.randomUUID();
    protected static Map<UUID, IResponseCallback>                                      responseCallbackHandlers = new HashMap<>();
    protected static Map<Class<? extends IRequest>, Class<? extends IRequestCallback>> requestCallbackHandlers  = new HashMap<>();

    @BeforeClass
    public static void setUp() {
        objectDataReplyHandler = new ObjectDataReplyHandler(null);

        responseCallback = new DummyNetworkHandler(null, null, null);
        responseCallbackHandlers.put(responseCallbackUUID, responseCallback);

        requestCallbackHandlers.put(DummyRequest.class, DummyRequestHandler.class);


    }

    @Test
    public void testAccessors() {
        objectDataReplyHandler.addRequestCallbackHandler(DummyRequest.class, DummyRequestHandler.class);
        assertTrue("DummyRequest should be registered", objectDataReplyHandler.getRequestCallbackHandlers().containsKey(DummyRequest.class));
        assertEquals("DummyRequestHandler should be registered as callback", DummyRequestHandler.class, objectDataReplyHandler.getRequestCallbackHandlers().get(DummyRequest.class));

        objectDataReplyHandler.removeRequestCallbackHandler(DummyRequest.class);
        assertTrue("DummyRequest should be unregistered", objectDataReplyHandler.getRequestCallbackHandlers().isEmpty());

        objectDataReplyHandler.addResponseCallbackHandler(responseCallbackUUID, responseCallback);
        assertTrue("Response should be registered", objectDataReplyHandler.getResponseCallbackHandlers().containsKey(responseCallbackUUID));
        assertEquals("ResponseCallback should be registered", responseCallback, objectDataReplyHandler.getResponseCallbackHandlers().get(responseCallbackUUID));

        objectDataReplyHandler.removeResponseCallbackHandler(responseCallbackUUID);
        assertTrue("Response should be unregistered", objectDataReplyHandler.getResponseCallbackHandlers().isEmpty());
    }

    @Test
    public void testConstruct() {
        objectDataReplyHandler = new ObjectDataReplyHandler(null, responseCallbackHandlers, requestCallbackHandlers);
        assertEquals("Response Callbacks are not equal", responseCallbackHandlers, objectDataReplyHandler.getResponseCallbackHandlers());
        assertEquals("Request Callbacks are not equal", requestCallbackHandlers, objectDataReplyHandler.getRequestCallbackHandlers());
    }
}

package org.rmatil.sync.network.test.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.INode;
import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.Node;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.network.test.core.base.BaseTest;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class NetworkHandlerTest extends BaseTest {

    protected static IUser user;

    protected static INodeManager clientManager1;
    protected static INodeManager clientManager2;

    protected static INode client1;
    protected static INode client2;

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException, InvalidKeyException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = gen.generateKeyPair();
        UUID clientDeviceId1 = UUID.randomUUID();
        UUID clientDeviceId2 = UUID.randomUUID();

        user = new User(
                "Cecil Hipplington-Shoreditch",
                "Lurch Schpellchek",
                "salt and pepper",
                keyPair.getPublic(),
                keyPair.getPrivate(),
                new ArrayList<>()
        );

        client1 = new Node(
                BaseTest.getTestConfig1(),
                user,
                clientDeviceId1
        );

        client2 = new Node(
                BaseTest.getTestConfig2(),
                user,
                clientDeviceId2
        );

        // Setup protocol
        ObjectDataReplyHandler objectDataReplyHandler1 = new ObjectDataReplyHandler(client1);
        objectDataReplyHandler1.addRequestCallbackHandler(DummyRequest.class, DummyRequestHandler.class);
        client1.setObjectDataReplyHandler(objectDataReplyHandler1);

        ObjectDataReplyHandler objectDataReplyHandler2 = new ObjectDataReplyHandler(client2);
        objectDataReplyHandler2.addRequestCallbackHandler(DummyRequest.class, DummyRequestHandler.class);
        client2.setObjectDataReplyHandler(objectDataReplyHandler2);

        client1.start();
        client2.start(client1.getPeerAddress().inetAddress().getHostAddress(), client1.getPeerAddress().tcpPort());

        clientManager1 = client1.getNodeManager();
        clientManager2 = client2.getNodeManager();
    }

    @AfterClass
    public static void tearDown() {
        client1.shutdown();
        client2.shutdown();
    }

    @Test
    public void testNetworkHandler()
            throws ExecutionException, InterruptedException {
        DummyNetworkHandler networkHandler = new DummyNetworkHandler(
                client1,
                clientManager1
        );

        boolean result = networkHandler.isCompleted();
        assertFalse("Result should be false", result);
        assertEquals("Progress should be 0", 0, networkHandler.getProgress());

        // this is normally invoked by an ExecutorService
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(networkHandler);

        // wait until all notified clients have responded
        networkHandler.await();

        assertTrue("NetworkHandler should be completed once all clients have responded", networkHandler.isCompleted());
        assertEquals("Progress should be 100%", 100, networkHandler.getProgress());

        assertTrue("Final result should be true", networkHandler.getResult());
    }
}

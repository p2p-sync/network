package org.rmatil.sync.network.test.core;

import net.tomp2p.futures.FutureDirect;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.IRequest;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.Node;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.ClientDevice;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.network.test.core.base.BaseTest;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class NodeTest extends BaseTest {

    protected static UUID c1Id = UUID.randomUUID();
    protected static UUID c2Id = UUID.randomUUID();
    protected static UUID c3Id = UUID.randomUUID();

    protected static Node nodeIpV6;
    protected static Node nodeIpV4_1;
    protected static Node nodeIpV4_2;

    protected static IUser user;
    protected static IUser user2;

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair1 = generator.generateKeyPair();
        KeyPair keyPair2 = generator.generateKeyPair();

        user = new User("Druid Wensleydale", "qwerty", "dictionaryAttack", keyPair1.getPublic(), keyPair1.getPrivate(), new ArrayList<>());
        user2 = new User("Archibald Northbottom", "letmein", "dictionaryAttack", keyPair2.getPublic(), keyPair2.getPrivate(), new ArrayList<>());

        nodeIpV6 = new Node(BaseTest.getTestConfig1(), user, c1Id);
        nodeIpV4_1 = new Node(BaseTest.getTestConfig2(), user, c2Id);
        nodeIpV4_2 = new Node(BaseTest.getTestConfig3(), user2, c3Id);
    }

    @After
    public void after()
            throws InterruptedException {
        nodeIpV6.shutdown();
        nodeIpV4_1.shutdown();
        nodeIpV4_2.shutdown();

        Thread.sleep(1000L);
    }

    @Test
    public void testClientId() {
        assertEquals("Id of client1 is not equal", c1Id, nodeIpV6.getClientDeviceId());
        assertEquals("Id of client2 is not equal", c2Id, nodeIpV4_1.getClientDeviceId());
        assertEquals("Id of client3 is not equal", c3Id, nodeIpV4_2.getClientDeviceId());
    }

    @Test
    public void testUser() {
        assertEquals("User should be the same", user, nodeIpV4_1.getUser());
        assertEquals("User should be the same", user2, nodeIpV4_2.getUser());
        assertEquals("User should be the same", user, nodeIpV6.getUser());
    }

    @Test
    public void testGetClientManager() {
        assertNull("NodeManager should be null before starting", nodeIpV6.getNodeManager());
        assertNull("IdentifierManager should be null before starting", nodeIpV6.getIdentifierManager());
        assertNull("UserManager should be null before starting", nodeIpV6.getUserManager());

        boolean succeededV6 = nodeIpV6.start();

        assertTrue("Node should be started", succeededV6);
        assertNotNull("NodeManager should be initialised after starting", nodeIpV6.getNodeManager());
        assertNotNull("IdentifierManager should be initialised after starting", nodeIpV6.getIdentifierManager());
        assertNotNull("UserManager should be initialised after starting", nodeIpV6.getUserManager());
    }

    @Test
    public void testBootstrapPeer()
            throws InterruptedException {
        boolean succeededV6 = nodeIpV6.start();
        boolean succeededV4_1 = nodeIpV4_1.start();
        boolean succeededV4_2 = nodeIpV4_2.start();

        assertTrue("IPv6 client did not succeed to start", succeededV6);
        assertTrue("IPv4_1 client did not succeed to start", succeededV4_1);
        assertTrue("IPv4_2 client did not succeed to start", succeededV4_2);
    }

    @Test
    public void testStartPeer()
            throws InterruptedException {
        // must be on different ports
        assertFalse("Node should not be connected before started", nodeIpV4_1.isConnected());
        boolean succeededV4_1 = nodeIpV4_1.start();
        assertTrue("Node should be connected after starting", nodeIpV4_1.isConnected());

        assertTrue("IPv4_1 client did not succeed to start", succeededV4_1);

        NodeLocation ipV4_1Client = new NodeLocation(UUID.randomUUID(), nodeIpV4_1.getPeerAddress());

        boolean succeededV4 = nodeIpV4_2.start(ipV4_1Client.getIpAddress(), ipV4_1Client.getPort());
        assertTrue("IPv4 2 client did not succeed to start", succeededV4);

        nodeIpV4_2.shutdown();
    }

    @Test
    public void testSendDirect()
            throws InterruptedException, IOException, ClassNotFoundException {

        UUID exchangeId = UUID.randomUUID();

        // Set up "Protocol"
        ObjectDataReplyHandler replyHandler1 = new ObjectDataReplyHandler(nodeIpV4_1);
        replyHandler1.addRequestCallbackHandler(DummyRequest.class, DummyRequestHandler.class);

        ObjectDataReplyHandler replyHandler2 = new ObjectDataReplyHandler(nodeIpV4_2);
        replyHandler2.addRequestCallbackHandler(DummyRequest.class, DummyRequestHandler.class);

        // set object data reply handler on both sides
        nodeIpV4_1.setObjectDataReplyHandler(replyHandler1);
        nodeIpV4_2.setObjectDataReplyHandler(replyHandler2);

        boolean c1 = nodeIpV4_1.start();
        boolean c2 = nodeIpV4_2.start();

        assertTrue("Client1 did not succeed to start", c1);
        assertTrue("Client2 did not succeed to start", c2);

        List<NodeLocation> receiver = new ArrayList<>();
        receiver.add(new NodeLocation(
                nodeIpV4_2.getClientDeviceId(),
                nodeIpV4_2.getPeerAddress()
        ));

        IRequest request = new DummyRequest(
                exchangeId,
                new ClientDevice(
                        nodeIpV4_1.getUser().getUserName(),
                        nodeIpV4_1.getClientDeviceId(),
                        nodeIpV4_1.getPeerAddress()
                ),
                receiver
        );

        CountDownLatch latch = new CountDownLatch(1);

        // now we wait on client 1 for the response
        replyHandler1.addResponseCallbackHandler(exchangeId, response -> {
            assertEquals("ExchangeId returned should be the same as sent", exchangeId, response.getExchangeId());
            nodeIpV4_1.getObjectDataReplyHandler().getResponseCallbackHandlers().remove(response.getExchangeId());
            latch.countDown();
        });

        assertEquals("Size of callbackHandler should be one, since the response was not yet processed", 1, nodeIpV4_1.getObjectDataReplyHandler().getResponseCallbackHandlers().size());

        FutureDirect futureDirect = nodeIpV4_1.sendDirect(request.getReceiverAddresses().iterator().next().getPeerAddress(), request);
        futureDirect.await();

        assertFalse("Future Direct should not have been failed (" + futureDirect.failedReason() + ")", futureDirect.isFailed());

        Object returnedObject = futureDirect.object();
        assertNull("Object returned by return handler should be null", returnedObject);

        // wait until response has been received
        latch.await();

        assertEquals("Size of callbackHandler should be zero, since the response was processed", 0, nodeIpV4_1.getObjectDataReplyHandler().getResponseCallbackHandlers().size());
    }
}

package org.rmatil.sync.network.test.core;

import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.Client;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.model.User;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

public class ClientTest {

    protected static UUID c1Id = UUID.randomUUID();
    protected static UUID c2Id = UUID.randomUUID();
    protected static UUID c3Id = UUID.randomUUID();

    protected static Client clientIpV6;
    protected static Client clientIpV4_1;
    protected static Client clientIpV4_2;

    protected static IUser user;
    protected static IUser user2;

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
        KeyPair keyPair1 = generator.generateKeyPair();
        KeyPair keyPair2 = generator.generateKeyPair();

        user = new User("Druid Wensleydale", "qwerty", "dictionaryAttack", keyPair1.getPublic(), keyPair1.getPrivate(), new ArrayList<>());
        user2 = new User("Archibald Northbottom", "letmein", "dictionaryAttack", keyPair2.getPublic(), keyPair2.getPrivate(), new ArrayList<>());

        clientIpV6 = new Client(Config.IPv6, user, c1Id);
        clientIpV4_1 = new Client(Config.IPv4, user, c2Id);
        clientIpV4_2 = new Client(Config.IPv4_2, user2, c3Id);
    }

    @After
    public void after()
            throws InterruptedException {
        clientIpV6.shutdown();
        clientIpV4_1.shutdown();
        clientIpV4_2.shutdown();

        Thread.sleep(1000L);
    }

    @Test
    public void testClientId() {
        assertEquals("Id of client1 is not equal", c1Id, clientIpV6.getClientDeviceId());
        assertEquals("Id of client2 is not equal", c2Id, clientIpV4_1.getClientDeviceId());
        assertEquals("Id of client3 is not equal", c3Id, clientIpV4_2.getClientDeviceId());
    }

    @Test
    public void testUser() {
        assertEquals("User should be the same", user, clientIpV4_1.getUser());
        assertEquals("User should be the same", user2, clientIpV4_2.getUser());
        assertEquals("User should be the same", user, clientIpV6.getUser());
    }

    @Test
    public void testBootstrapPeer()
            throws InterruptedException {
        boolean succeededV6 = clientIpV6.start();
        boolean succeededV4_1 = clientIpV4_1.start();
        boolean succeededV4_2 = clientIpV4_2.start();

        assertTrue("IPv6 client did not succeed to start", succeededV6);
        assertTrue("IPv4_1 client did not succeed to start", succeededV4_1);
        assertTrue("IPv4_2 client did not succeed to start", succeededV4_2);
    }

    @Test
    public void testStartPeer()
            throws InterruptedException {
        // must be on different ports
        assertNull("PeerDHT should be null before starting", clientIpV4_1.getPeerDht());
        boolean succeededV4_1 = clientIpV4_1.start();
        assertNotNull("PeerDHT should not be null after starting", clientIpV4_1.getPeerDht());

        assertTrue("IPv4_1 client did not succeed to start", succeededV4_1);

        ClientLocation ipV4_1Client = new ClientLocation(UUID.randomUUID(), clientIpV4_1.getPeerAddress());

        boolean succeededV4 = clientIpV4_2.start(ipV4_1Client.getIpAddress(), ipV4_1Client.getPort());
        assertTrue("IPv4 2 client did not succeed to start", succeededV4);

        clientIpV4_2.shutdown();
    }

    @Test
    public void testSendDirect()
            throws InterruptedException, IOException, ClassNotFoundException {
        String sentObject = "A coffee a day gets the doctor away";

        ObjectDataReplyHandler replyHandler = new ObjectDataReplyHandler();
        replyHandler.addObjectDataReply(String.class, (sender, request) -> {
            assertEquals("Object is not equals", sentObject, request);

            return sentObject;
        });

        clientIpV4_1.setObjectDataReplyHandler(replyHandler);

        boolean c1 = clientIpV4_1.start();
        boolean c2 = clientIpV4_2.start();

        assertTrue("Client1 did not succeed to start", c1);
        assertTrue("Client2 did not succeed to start", c2);

        PeerAddress address = clientIpV4_1.getPeerAddress();

        FutureDirect futureDirect = clientIpV4_2.sendDirect(address, sentObject);
        futureDirect.await();

        assertFalse("Future Direct should not have been failed", futureDirect.isFailed());

        Object returnedObject = futureDirect.object();
        assertEquals("Object returned by return handler is not equal", sentObject, returnedObject);
    }
}

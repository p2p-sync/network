package org.rmatil.sync.network.test.core;

import net.tomp2p.dht.PeerDHT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.NodeManager;
import org.rmatil.sync.network.core.Connection;
import org.rmatil.sync.network.core.UserManager;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.network.test.core.base.BaseTest;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

public class UserManagerTest {

    protected static PeerDHT peer1;
    protected static PeerDHT peer2;
    protected static PeerDHT peer3;

    protected static IStorageAdapter dhtStorageAdapter1;
    protected static IStorageAdapter dhtStorageAdapter2;
    protected static IStorageAdapter dhtStorageAdapter3;

    protected static KeyPair keyPair1;
    protected static KeyPair keyPair2;

    protected static INodeManager clientManager1;
    protected static INodeManager clientManager2;
    protected static INodeManager clientManager3;

    protected static IUser        user1;
    protected static IUser        user2;
    protected static NodeLocation l1;
    protected static NodeLocation l2;
    protected static NodeLocation l3;

    protected static UserManager userManager1;
    protected static UserManager userManager2;
    protected static UserManager userManager3;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        // restart on each test since the public key is not removed
        // on logout

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        keyPair1 = generator.genKeyPair();
        keyPair2 = generator.genKeyPair();

        user1 = new User("Hermann P. Schnitzel", "admin123", "dictionaryAttack", keyPair1.getPublic(), keyPair1.getPrivate(), new ArrayList<>());
        user2 = new User("Ruby von Rails", "123456", "dictionaryAttack", keyPair2.getPublic(), keyPair2.getPrivate(), new ArrayList<>());

        // bootstrap peer and client of user1
        Connection con1 = new Connection(BaseTest.getTestConfig1(), null);
        con1.open(user1.getKeyPair());
        peer1 = con1.getPeerDHT();

        // client 2 of user1
        Connection con2 = new Connection(BaseTest.getTestConfig2(), null);
        con2.open(user1.getKeyPair());
        con2.connect(con1.getPeerDHT().peerAddress().inetAddress().getHostAddress(), con1.getPeerDHT().peerAddress().tcpPort());
        peer2 = con2.getPeerDHT();

        // client 1 of user2
        Connection con3 = new Connection(BaseTest.getTestConfig3(), null);
        con3.open(user2.getKeyPair());
        con3.connect(con1.getPeerDHT().peerAddress().inetAddress().getHostAddress(), con1.getPeerDHT().peerAddress().tcpPort());
        peer3 = con3.getPeerDHT();

        l1 = new NodeLocation(UUID.randomUUID(), peer1.peerAddress());
        l2 = new NodeLocation(UUID.randomUUID(), peer2.peerAddress());
        l3 = new NodeLocation(UUID.randomUUID(), peer3.peerAddress());

        dhtStorageAdapter1 = new DhtStorageAdapter(peer1);
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2);
        dhtStorageAdapter3 = new DhtStorageAdapter(peer3);

        clientManager1 = new NodeManager(
                dhtStorageAdapter1,
                Config.DEFAULT.getLocationsContentKey(),
                Config.DEFAULT.getPrivateKeyContentKey(),
                Config.DEFAULT.getPublicKeyContentKey(),
                Config.DEFAULT.getSaltContentKey(),
                Config.DEFAULT.getDomainKey()
        );

        clientManager2 = new NodeManager(
                dhtStorageAdapter2,
                Config.DEFAULT.getLocationsContentKey(),
                Config.DEFAULT.getPrivateKeyContentKey(),
                Config.DEFAULT.getPublicKeyContentKey(),
                Config.DEFAULT.getSaltContentKey(),
                Config.DEFAULT.getDomainKey()
        );
        clientManager3 = new NodeManager(
                dhtStorageAdapter3,
                Config.DEFAULT.getLocationsContentKey(),
                Config.DEFAULT.getPrivateKeyContentKey(),
                Config.DEFAULT.getPublicKeyContentKey(),
                Config.DEFAULT.getSaltContentKey(),
                Config.DEFAULT.getDomainKey()
        );

        userManager1 = new UserManager(
                clientManager1,
                l1
        );

        userManager2 = new UserManager(
                clientManager2,
                l2
        );

        userManager3 = new UserManager(
                clientManager3,
                l3
        );

    }

    @After
    public void tearDown()
            throws InterruptedException {
        // restart on each test since the public key is not removed
        // on logout
        peer1.shutdown();
        peer2.shutdown();
        peer3.shutdown();

        Thread.sleep(1000L);
    }

    @Test
    public void test()
            throws InputOutputException {
        assertFalse("User1 should not yet be registered", userManager1.isRegistered(user1.getUserName()));
        assertFalse("User1 should not yet be registered", userManager2.isRegistered(user1.getUserName()));
        assertFalse("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));

        clientManager1.addPublicKey(user1);

        assertTrue("User1 should be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should be registered", userManager2.isRegistered(user1.getUserName()));
        assertFalse("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));

        clientManager3.addPublicKey(user2);

        assertTrue("User1 should be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should be registered", userManager2.isRegistered(user1.getUserName()));
        assertTrue("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));
    }

    @Test
    public void testLoginLogout()
            throws InputOutputException {
        assertFalse("User1 should not yet be registered", userManager1.isRegistered(user1.getUserName()));
        assertFalse("User1 should not yet be registered", userManager2.isRegistered(user1.getUserName()));
        assertFalse("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));

        userManager1.login(user1);

        assertTrue("User1 should be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should be registered", userManager2.isRegistered(user1.getUserName()));
        assertFalse("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));

        // add the 2nd client location to user1 too
        userManager2.login(user1);

        assertTrue("User1 should be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should be registered", userManager2.isRegistered(user1.getUserName()));
        assertFalse("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));

        userManager3.login(user2);

        assertTrue("User1 should be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should be registered", userManager2.isRegistered(user1.getUserName()));
        assertTrue("User2 should not yet be registered", userManager3.isRegistered(user2.getUserName()));

        // this should not affect any client location since
        // the user1's client does not have write access
        userManager1.logout(user2);

        assertEquals("ClientLocations should not be removed", 1, clientManager3.getNodeLocations(user2).size());

        assertTrue("User1 should be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should be registered", userManager2.isRegistered(user1.getUserName()));
        assertTrue("User2 should be registered", userManager3.isRegistered(user2.getUserName()));

        userManager2.logout(user1);

        assertEquals("Only one client location of user1 should exist", 1, clientManager1.getNodeLocations(user1).size());

        assertTrue("User1 should still be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should still be registered", userManager2.isRegistered(user1.getUserName()));
        assertTrue("User2 should be registered", userManager3.isRegistered(user2.getUserName()));

        userManager1.logout(user1);

        // all users should still be registered since logout should not remove the public key
        assertTrue("User1 should still be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should still be registered", userManager2.isRegistered(user1.getUserName()));
        assertTrue("User2 should still be registered", userManager3.isRegistered(user2.getUserName()));

        userManager3.logout(user2);

        // all users should still be registered since logout should not remove the public key
        assertTrue("User1 should still be registered", userManager1.isRegistered(user1.getUserName()));
        assertTrue("User1 should still be registered", userManager2.isRegistered(user1.getUserName()));
        assertTrue("User2 should still be registered", userManager3.isRegistered(user2.getUserName()));

        assertEquals("ClientLocations should not be removed", 0, clientManager1.getNodeLocations(user1).size());
        assertEquals("ClientLocations should not be removed", 0, clientManager2.getNodeLocations(user1).size());
        assertEquals("ClientLocations should not be removed", 0, clientManager3.getNodeLocations(user2).size());
    }
}
package org.rmatil.sync.network.test.core;

import net.tomp2p.dht.PeerDHT;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.Connection;
import org.rmatil.sync.network.core.NodeManager;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.network.test.core.base.BaseTest;
import org.rmatil.sync.persistence.core.dht.secured.ISecuredDhtStorageAdapter;
import org.rmatil.sync.persistence.core.dht.secured.SecuredDhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class NodeManagerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static PeerDHT peer1;
    protected static PeerDHT peer2;
    protected static PeerDHT peer3;

    protected static ISecuredDhtStorageAdapter dhtStorageAdapter1;
    protected static ISecuredDhtStorageAdapter dhtStorageAdapter2;
    protected static ISecuredDhtStorageAdapter dhtStorageAdapter3;

    protected static KeyPair keyPair1;
    protected static KeyPair keyPair2;

    protected static INodeManager clientManager1;
    protected static INodeManager clientManager2;
    protected static INodeManager clientManager3;

    protected static IUser        user1;
    protected static IUser        user2;
    protected static NodeLocation l1;
    protected static NodeLocation l2;


    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        keyPair1 = generator.genKeyPair();
        keyPair2 = generator.genKeyPair();

        user1 = new User("Hermann P. Schnitzel", "admin123", "dictionaryAttack", keyPair1.getPublic(), keyPair1.getPrivate(), new ArrayList<>());
        user2 = new User("Ruby von Rails", "123456", "dictionaryAttack", keyPair2.getPublic(), keyPair2.getPrivate(), new ArrayList<>());

        /// bootstrap peer and client of user1
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

        l1 = new NodeLocation(user1.getUserName(), UUID.randomUUID(), peer1.peerAddress());
        l2 = new NodeLocation(user1.getUserName(), UUID.randomUUID(), peer2.peerAddress());

        dhtStorageAdapter1 = new SecuredDhtStorageAdapter(peer1);
        dhtStorageAdapter2 = new SecuredDhtStorageAdapter(peer2);
        dhtStorageAdapter3 = new SecuredDhtStorageAdapter(peer3);

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

    }

    @AfterClass
    public static void tearDown() {
        peer1.shutdown();
        peer2.shutdown();
        peer3.shutdown();
    }

    @After
    public void after()
            throws InputOutputException {
        clientManager1.removeNodeLocation(l1);
        clientManager1.removeNodeLocation(l2);
    }

    @Test
    public void testAddLocation()
            throws InputOutputException {
        clientManager1.addNodeLocation(l1);

        List<NodeLocation> result = clientManager1.getNodeLocations(user1.getUserName());
        List<NodeLocation> result2 = clientManager2.getNodeLocations(user1.getUserName());

        // we should also be able to fetch the locations from a different user's client
        IUser tmpUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), null, user1.getNodeLocations());
        List<NodeLocation> result3 = clientManager3.getNodeLocations(tmpUser.getUserName());

        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));
        assertEquals("Result2 has different amount of locations saved", 1, result2.size());
        assertThat("result2 does not contain location", result2, hasItem(l1));
        assertEquals("Result3 has different amount of locations saved", 1, result3.size());
        assertThat("result3 does not contain location", result3, hasItem(l1));
    }

    @Test
    public void testRemoveLocation()
            throws InputOutputException {
        clientManager1.addNodeLocation(l1);

        List<NodeLocation> result = clientManager1.getNodeLocations(user1.getUserName());
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        // we should be unable to remove a location from a different user's client
        IUser tmpUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), null, user1.getNodeLocations());
        clientManager3.removeNodeLocation(l1);

        // this should still have the result in it since the clientManager3 has a different user
        // and therefore a different private key -> no access to delete
        List<NodeLocation> result3 = clientManager1.getNodeLocations(user1.getUserName());
        assertEquals("Result has different amount of locations saved", 1, result3.size());
        assertThat("result does not contain location", result3, hasItem(l1));

        // this should remove the location since the client has the same public key pair of the same user
        clientManager2.removeNodeLocation(l1);
        List<NodeLocation> result2 = clientManager1.getNodeLocations(user1.getUserName());
        assertEquals("Result should be empty after removal of other client", 0, result2.size());
    }

    @Test
    public void testGetLocations()
            throws InputOutputException {
        clientManager1.addNodeLocation(l1);
        clientManager1.addNodeLocation(l2);

        List<NodeLocation> result = clientManager1.getNodeLocations(user1.getUserName());
        List<NodeLocation> result2 = clientManager2.getNodeLocations(user1.getUserName());
        List<NodeLocation> result3 = clientManager3.getNodeLocations(user1.getUserName());
        assertEquals("Result has not both locations in it", 2, result.size());
        assertThat("Result does not contain both locations", result, hasItems(l1, l2));
        assertEquals("Result2 has not both locations in it", 2, result2.size());
        assertThat("Result2 does not contain both locations", result2, hasItems(l1, l2));
        assertEquals("Result3 has not both locations in it", 2, result3.size());
        assertThat("Result3 does not contain both locations", result3, hasItems(l1, l2));
    }

    @Test
    public void testAccessForOtherUsers()
            throws InputOutputException {
        clientManager1.addNodeLocation(l1);

        // location manager of user2 wants to receive locations
        // from user1
        List<NodeLocation> result = clientManager3.getNodeLocations(user1.getUserName());
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        result = clientManager3.getNodeLocations(user1.getUserName());
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        // user2 wants to persist a location for user1, but got only the publickey
        // since he is not the correct owner, he can not persist anything
        // owner := hash(public_key) == domain
        // e.g Owner of domain 0x1234 is peer where 0x1234 == hash(public_key)
        clientManager3.addNodeLocation(l2);

        List<NodeLocation> resultAfterAdding = clientManager1.getNodeLocations(user1.getUserName());
        assertEquals("Result has different amount of locations saved", 1, resultAfterAdding.size());
        assertThat("Result does not contain location l1", resultAfterAdding, hasItem(l1));
        assertThat("result should not contain location l2", resultAfterAdding, not(hasItem(l2)));


        clientManager1.addNodeLocation(l2);

        // we should be able to get all locations from another user
        IUser otherUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), null, user1.getNodeLocations());
        List<NodeLocation> result2 = clientManager3.getNodeLocations(otherUser.getUserName());
        assertEquals("Result has different amount of locations saved", 2, result2.size());
        assertThat("Result does not contain location l1", result2, hasItem(l1));
        assertThat("result should not contain location l2", result2, hasItem(l1));
    }

    @Test
    public void testAddAndGetPrivateKey()
            throws InputOutputException {
        clientManager1.addPrivateKey(user1);

        PrivateKey fetchedPr = clientManager1.getPrivateKey(user1);
        PrivateKey fetchedPr2 = clientManager2.getPrivateKey(user1);

        assertArrayEquals("Private keys are not the same", user1.getPrivateKey().getEncoded(), fetchedPr.getEncoded());
        assertArrayEquals("Private keys are not the same (2)", user1.getPrivateKey().getEncoded(), fetchedPr2.getEncoded());

        // other user should not be able to overwrite private key of another user
        clientManager3.addPrivateKey(user1);
        IUser anotherUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), user2.getPrivateKey(), user1.getNodeLocations());
        PrivateKey fetchedPr3 = clientManager1.getPrivateKey(anotherUser);
        assertNull("Fetched private key of other user should be null", fetchedPr3);
    }

    @Test
    public void testAndGetPublicKey()
            throws InputOutputException {
        clientManager1.addPublicKey(user1);

        PublicKey fetchedPk = clientManager1.getPublicKey(user1);
        PublicKey fetchedPk1 = clientManager1.getPublicKey(user1.getUserName());
        PublicKey fetchedPk2 = clientManager2.getPublicKey(user1);

        // first assure, that the clients of the same user are able to get the public key
        assertArrayEquals("Fetched public key is not the same (1)", user1.getPublicKey().getEncoded(), fetchedPk.getEncoded());
        assertArrayEquals("Fetched public key is not the same (2)", user1.getPublicKey().getEncoded(), fetchedPk1.getEncoded());
        assertArrayEquals("Fetched public key is not the same (3)", user1.getPublicKey().getEncoded(), fetchedPk2.getEncoded());

        IUser anotherUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user2.getPublicKey(), user2.getPrivateKey(), user1.getNodeLocations());
        PublicKey fetchedPk3 = clientManager3.getPublicKey(anotherUser);

        assertNotNull("Other client should be able to fetch public key of user1", fetchedPk3);
        assertArrayEquals("fetched public key is not the same (3)", user1.getPublicKey().getEncoded(), fetchedPk3.getEncoded());
    }

    @Test
    public void testSetAndGetSalt()
            throws InputOutputException {
        clientManager1.addSalt(user1);

        String fetchedSalt = clientManager1.getSalt(user1);
        String fetchedSalt2 = clientManager2.getSalt(user1);

        assertEquals("Salt is not equals", user1.getSalt(), fetchedSalt);
        assertEquals("Salt2 is not equals", user1.getSalt(), fetchedSalt2);

        String emptySalt = clientManager3.getSalt(user2);
        assertNull("Salt should be null since never stored", emptySalt);
    }

}

package org.rmatil.sync.network.test.core;

import net.tomp2p.dht.PeerDHT;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.ClientManager;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ClientManagerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static PeerDHT peer1;
    protected static PeerDHT peer2;
    protected static PeerDHT peer3;

    protected static IStorageAdapter dhtStorageAdapter1;
    protected static IStorageAdapter dhtStorageAdapter2;
    protected static IStorageAdapter dhtStorageAdapter3;

    protected static KeyPair keyPair1;
    protected static KeyPair keyPair2;

    protected static IClientManager clientManager1;
    protected static IClientManager clientManager2;
    protected static IClientManager clientManager3;

    protected static IUser          user1;
    protected static IUser          user2;
    protected static ClientLocation l1;
    protected static ClientLocation l2;


    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
        keyPair1 = generator.genKeyPair();
        keyPair2 = generator.genKeyPair();

        user1 = new User("Hermann P. Schnitzel", "admin123", "dictionaryAttack", keyPair1.getPublic(), keyPair1.getPrivate(), new ArrayList<>());
        user2 = new User("Ruby von Rails", "123456", "dictionaryAttack", keyPair2.getPublic(), keyPair2.getPrivate(), new ArrayList<>());

        // bootstrap peer and client of user1
        peer1 = PeerDhtUtils.initPeerDht(org.rmatil.sync.network.config.Config.IPv4, user1);
        // client 2 of user1
        peer2 = PeerDhtUtils.initPeerDht(org.rmatil.sync.network.config.Config.IPv4_2, user1);

        // client 1 of user2
        peer3 = PeerDhtUtils.initPeerDht(org.rmatil.sync.network.config.Config.IPv4_3, user2);

        l1 = new ClientLocation(UUID.randomUUID(), peer1.peerAddress());

        PeerDhtUtils.connectToLocation(peer2, l1);
        l2 = new ClientLocation(UUID.randomUUID(), peer2.peerAddress());
        PeerDhtUtils.connectToLocation(peer3, l1);

        dhtStorageAdapter1 = new DhtStorageAdapter(peer1);
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2);
        dhtStorageAdapter3 = new DhtStorageAdapter(peer3);

        clientManager1 = new ClientManager(
                dhtStorageAdapter1,
                org.rmatil.sync.network.config.Config.IPv4.getLocationsContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getPrivateKeyContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getPublicKeyContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getSaltContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getDomainKey()
        );
        clientManager2 = new ClientManager(
                dhtStorageAdapter2,
                org.rmatil.sync.network.config.Config.IPv4_2.getLocationsContentKey(),
                org.rmatil.sync.network.config.Config.IPv4_2.getPrivateKeyContentKey(),
                org.rmatil.sync.network.config.Config.IPv4_2.getPublicKeyContentKey(),
                org.rmatil.sync.network.config.Config.IPv4_2.getSaltContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getDomainKey()
        );
        clientManager3 = new ClientManager(
                dhtStorageAdapter3,
                org.rmatil.sync.network.config.Config.IPv4_3.getLocationsContentKey(),
                org.rmatil.sync.network.config.Config.IPv4_3.getPrivateKeyContentKey(),
                org.rmatil.sync.network.config.Config.IPv4_3.getPublicKeyContentKey(),
                org.rmatil.sync.network.config.Config.IPv4_3.getSaltContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getDomainKey()
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
        clientManager1.removeClientLocation(user1, l1);
        clientManager1.removeClientLocation(user1, l2);
    }

    @Test
    public void testAddLocation()
            throws InputOutputException {
        clientManager1.addClientLocation(user1, l1);

        List<ClientLocation> result = clientManager1.getClientLocations(user1);
        List<ClientLocation> result2 = clientManager2.getClientLocations(user1);

        // we should also be able to fetch the locations from a different user's client
        IUser tmpUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), null, user1.getClientLocations());
        List<ClientLocation> result3 = clientManager3.getClientLocations(tmpUser);

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
        clientManager1.addClientLocation(user1, l1);

        List<ClientLocation> result = clientManager1.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        // we should be unable to remove a location from a different user's client
        IUser tmpUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), null, user1.getClientLocations());
        clientManager3.removeClientLocation(tmpUser, l1);

        // this should still have the result in it since the clientManager3 has a different user
        // and therefore a different private key -> no access to delete
        List<ClientLocation> result3 = clientManager1.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 1, result3.size());
        assertThat("result does not contain location", result3, hasItem(l1));

        // this should remove the location since the client has the same public key pair of the same user
        clientManager2.removeClientLocation(user1, l1);
        List<ClientLocation> result2 = clientManager1.getClientLocations(user1);
        assertEquals("Result should be empty after removal of other client", 0, result2.size());
    }

    @Test
    public void testGetLocations()
            throws InputOutputException {
        clientManager1.addClientLocation(user1, l1);
        clientManager1.addClientLocation(user1, l2);

        List<ClientLocation> result = clientManager1.getClientLocations(user1);
        List<ClientLocation> result2 = clientManager2.getClientLocations(user1);
        List<ClientLocation> result3 = clientManager3.getClientLocations(user1);
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
        clientManager1.addClientLocation(user1, l1);

        // location manager of user2 wants to receive locations
        // from user1
        List<ClientLocation> result = clientManager3.getClientLocations(new User(
                user1.getUserName(),
                "asdf",
                "asf",
                null,
                null,
                new ArrayList<>()
        ));
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        result = clientManager3.getClientLocations(user1.getUserName());
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        // user2 wants to persist a location for user1, but got only the publickey
        // since he is not the correct owner, he can not persist anything
        // owner := hash(public_key) == domain
        // e.g Owner of domain 0x1234 is peer where 0x1234 == hash(public_key)
        clientManager3.addClientLocation(user2, l2);

        List<ClientLocation> resultAfterAdding = clientManager1.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 1, resultAfterAdding.size());
        assertThat("Result does not contain location l1", resultAfterAdding, hasItem(l1));
        assertThat("result should not contain location l2", resultAfterAdding, not(hasItem(l2)));


        clientManager1.addClientLocation(user1, l2);

        // we should be able to get all locations from another user
        IUser otherUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), null, user1.getClientLocations());
        List<ClientLocation> result2 = clientManager3.getClientLocations(otherUser);
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
        IUser anotherUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user1.getPublicKey(), user2.getPrivateKey(), user1.getClientLocations());
        PrivateKey fetchedPr3 = clientManager1.getPrivateKey(anotherUser);
        assertNull("Fetched private key of other user should be null", fetchedPr3);
    }

    @Test
    public void testAndGetPublicKey()
            throws InputOutputException {
        clientManager1.addPublicKey(user1);

        PublicKey fetchedPk = clientManager1.getPublicKey(user1);
        PublicKey fetchedPk2 = clientManager2.getPublicKey(user1);

        // first assure, that the clients of the same user are able to get the public key
        assertArrayEquals("Fetched public key is not the same (1)", user1.getPublicKey().getEncoded(), fetchedPk.getEncoded());
        assertArrayEquals("Fetched public key is not the same (2)", user1.getPublicKey().getEncoded(), fetchedPk2.getEncoded());

        IUser anotherUser = new User(user1.getUserName(), "someWrongPassword", "dictionaryAttack", user2.getPublicKey(), user2.getPrivateKey(), user1.getClientLocations());
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

package org.rmatil.sync.network.test.core;

import net.tomp2p.dht.PeerDHT;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.api.IIdentifierManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.IdentifierManager;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class IdentifierManagerTest {

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

    protected static IIdentifierManager<String> identifierManager;
    protected static IIdentifierManager<String> identifierManager2;
    protected static IIdentifierManager<String> identifierManager3;

    protected static IUser          user1;
    protected static IUser          user2;
    protected static ClientLocation l1;
    protected static ClientLocation l2;

    protected static final UUID   KEY_1   = UUID.randomUUID();
    protected static final String VALUE_1 = "Hello there!";

    protected static final UUID   KEY_2   = UUID.randomUUID();
    protected static final String VALUE_2 = "Ou, look who's here!";

    protected static final UUID   KEY_3   = UUID.randomUUID();
    protected static final String VALUE_3 = "How are you?";

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

        identifierManager = new IdentifierManager(
                dhtStorageAdapter1,
                user1.getUserName(),
                org.rmatil.sync.network.config.Config.IPv4.getIdentifieContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getDomainKey()
        );
        identifierManager2 = new IdentifierManager(
                dhtStorageAdapter2,
                user1.getUserName(),
                org.rmatil.sync.network.config.Config.IPv4.getIdentifieContentKey(),
                org.rmatil.sync.network.config.Config.IPv4.getDomainKey()
        );
        identifierManager3 = new IdentifierManager(
                dhtStorageAdapter3,
                user2.getUserName(),
                org.rmatil.sync.network.config.Config.IPv4.getIdentifieContentKey(),
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
        identifierManager.removeIdentifier(KEY_1);
        identifierManager.removeIdentifier(KEY_2);
        identifierManager.removeIdentifier(KEY_3);
    }

    @Test
    public void testAddIdentifier()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);

        // user 1
        String result = identifierManager.getIdentifierValue(KEY_1);
        String result2 = identifierManager2.getIdentifierValue(KEY_1);

        // since this manager is from a different user,
        // no key is stored
        String result3 = identifierManager3.getIdentifierValue(KEY_1);

        assertNotNull("Result should not be null", result);
        assertEquals("Result should be equal", VALUE_1, result);
        assertNotNull("Result should not be null", result2);
        assertEquals("Result should be equal", VALUE_1, result2);
        assertNull("Result should not null", result3);
    }

    @Test
    public void testRemoveIdentifier()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);

        String result = identifierManager.getIdentifierValue(KEY_1);
        assertEquals("Result should be equal", VALUE_1, result);

        // we should be unable to remove a location from a different user's client
        // -> this would try to delete KEY_1 at user2's identifier store
        identifierManager3.removeIdentifier(KEY_1);

        // this should still have the result in it since the clientManager3 has a different user
        // and therefore a different private key -> no access to delete
        String result2 = identifierManager.getIdentifierValue(KEY_1);
        assertEquals("Result should still be equal after wrong delete request", VALUE_1, result2);

        // this should remove the location since the client has the same public key pair of the same user
        identifierManager2.removeIdentifier(KEY_1);
        String result3 = identifierManager.getIdentifierValue(KEY_1);
        assertNull("Result should be null after deletion", result3);
    }

    @Test
    public void testGetIdentifierMap()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);
        identifierManager.addIdentifier(KEY_2, VALUE_2);

        Map<UUID, String> result = identifierManager.getIdentifierMap();
        Map<UUID, String> result2 = identifierManager.getIdentifierMap();
        Map<UUID, String> result3 = identifierManager.getIdentifierMap();

        assertEquals("Result has not both locations in it", 2, result.size());
        assertEquals("Value1 should be contained", VALUE_1, result.get(KEY_1));
        assertEquals("Value2 should be contained", VALUE_2, result.get(KEY_2));

        assertEquals("Result has not both locations in it", 2, result2.size());
        assertEquals("Value1 should be contained", VALUE_1, result2.get(KEY_1));
        assertEquals("Value2 should be contained", VALUE_2, result2.get(KEY_2));

        assertEquals("Result has not both locations in it", 2, result3.size());
        assertEquals("Value1 should be contained", VALUE_1, result3.get(KEY_1));
        assertEquals("Value2 should be contained", VALUE_2, result3.get(KEY_2));
    }

    @Test
    public void testAccessForOtherUsers()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);

        // result should be null since user2 has not saved anything yet
        // in the DHT
        String result = identifierManager3.getIdentifierValue(KEY_1);
        assertNull("Result should be null", result);

        // add the key value to user2
        identifierManager3.addIdentifier(KEY_3, VALUE_3);

        String result2 = identifierManager.getIdentifierValue(KEY_3);
        assertNull("Result should be null since client1 has a different storage", result2);
    }
}

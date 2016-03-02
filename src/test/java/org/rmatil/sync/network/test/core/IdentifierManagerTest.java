package org.rmatil.sync.network.test.core;

import net.tomp2p.dht.PeerDHT;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.api.IIdentifierManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.Connection;
import org.rmatil.sync.network.core.IdentifierManager;
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

    protected static IIdentifierManager<String, UUID> identifierManager;
    protected static IIdentifierManager<String, UUID> identifierManager2;
    protected static IIdentifierManager<String, UUID> identifierManager3;

    protected static IUser        user1;
    protected static IUser        user2;
    protected static NodeLocation l1;
    protected static NodeLocation l2;

    protected static final String KEY_1   = "Hello there!";
    protected static final UUID   VALUE_1 = UUID.randomUUID();

    protected static final String KEY_2   = "Ou, look who's here!";
    protected static final UUID   VALUE_2 = UUID.randomUUID();

    protected static final String KEY_3   = "How are you?";
    protected static final UUID   VALUE_3 = UUID.randomUUID();

    protected static final String NON_EXISTING_KEY   = "Not existing?";
    protected static final UUID   NON_EXISTING_VALUE = UUID.randomUUID();

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException, IOException, InvalidKeyException {
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

        l1 = new NodeLocation(user1.getUserName(), UUID.randomUUID(), peer1.peerAddress());
        l2 = new NodeLocation(user2.getUserName(), UUID.randomUUID(), peer2.peerAddress());


        dhtStorageAdapter1 = new DhtStorageAdapter(peer1);
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2);
        dhtStorageAdapter3 = new DhtStorageAdapter(peer3);

        identifierManager = new IdentifierManager(
                dhtStorageAdapter1,
                user1.getUserName(),
                Config.DEFAULT.getIdentifierContentKey(),
                Config.DEFAULT.getDomainKey()
        );
        identifierManager2 = new IdentifierManager(
                dhtStorageAdapter2,
                user1.getUserName(),
                Config.DEFAULT.getIdentifierContentKey(),
                Config.DEFAULT.getDomainKey()
        );
        identifierManager3 = new IdentifierManager(
                dhtStorageAdapter3,
                user2.getUserName(),
                Config.DEFAULT.getIdentifierContentKey(),
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
        identifierManager.removeIdentifier(KEY_1);
        identifierManager.removeIdentifier(KEY_2);
        identifierManager.removeIdentifier(KEY_3);
    }

    @Test
    public void testAddIdentifier()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);

        // user 1
        UUID result = identifierManager.getValue(KEY_1);
        UUID result2 = identifierManager2.getValue(KEY_1);

        // since this manager is from a different user,
        // no key is stored
        UUID result3 = identifierManager3.getValue(KEY_1);

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

        UUID result = identifierManager.getValue(KEY_1);
        assertEquals("Result should be equal", VALUE_1, result);

        // we should be unable to remove a location from a different user's client
        // -> this would try to delete KEY_1 at user2's identifier store
        identifierManager3.removeIdentifier(KEY_1);

        // this should still have the result in it since the clientManager3 has a different user
        // and therefore a different private key -> no access to delete
        UUID result2 = identifierManager.getValue(KEY_1);
        assertEquals("Result should still be equal after wrong delete request", VALUE_1, result2);

        // this should remove the location since the client has the same public key pair of the same user
        identifierManager2.removeIdentifier(KEY_1);
        UUID result3 = identifierManager.getValue(KEY_1);
        assertNull("Result should be null after deletion", result3);
    }

    @Test
    public void testGetKeyAndGetValue()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);

        // user 1
        UUID value1 = identifierManager.getValue(KEY_1);
        UUID value2 = identifierManager2.getValue(KEY_1);

        assertNotNull("Result should not be null", value1);
        assertEquals("Result should be equal", VALUE_1, value1);
        assertNotNull("Result should not be null", value2);
        assertEquals("Result should be equal", VALUE_1, value2);

        String key1 = identifierManager.getKey(VALUE_1);
        String key2 = identifierManager2.getKey(VALUE_1);

        assertNotNull("Result should not be null", key1);
        assertEquals("Result should be equal", KEY_1, key1);
        assertNotNull("Result should not be null", key2);
        assertEquals("Result should be equal", KEY_1, key2);
    }

    @Test
    public void testGetNonExistingKeyAndValue()
            throws InputOutputException {
        UUID value = identifierManager.getValue(NON_EXISTING_KEY);
        String key = identifierManager.getKey(NON_EXISTING_VALUE);

        assertNull("Value should be null", value);
        assertNull("Key should be null", key);
    }

    @Test
    public void testAccessForOtherUsers()
            throws InputOutputException {
        identifierManager.addIdentifier(KEY_1, VALUE_1);

        // result should be null since user2 has not saved anything yet
        // in the DHT
        UUID result = identifierManager3.getValue(KEY_1);
        assertNull("Result should be null", result);

        // add the key value to user2
        identifierManager3.addIdentifier(KEY_3, VALUE_3);

        UUID result2 = identifierManager.getValue(KEY_3);
        assertNull("Result should be null since client1 has a different storage", result2);
    }
}

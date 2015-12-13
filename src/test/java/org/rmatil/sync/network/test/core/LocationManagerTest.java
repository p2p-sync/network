package org.rmatil.sync.network.test.core;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.StorageLayer;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rmatil.sync.network.api.ILocationManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.LocationManager;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.network.test.core.config.Config;
import org.rmatil.sync.persistence.api.IStorageAdapter;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class LocationManagerTest {

    protected final static Logger logger = LoggerFactory.getLogger(LocationManagerTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    protected static PeerDHT peer1;
    protected static PeerDHT peer2;

    protected static IStorageAdapter dhtStorageAdapter1;
    protected static IStorageAdapter dhtStorageAdapter2;

    protected static KeyPair keyPair1;
    protected static KeyPair keyPair2;

    protected static ILocationManager locationManager1;
    protected static ILocationManager locationManager2;

    protected static IUser user1;
    protected static IUser user2;
    protected static ClientLocation l1 = new ClientLocation("123.456.78.90", 4001);
    protected static ClientLocation l2 = new ClientLocation("098.765.43.21", 1234);


    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException, IOException {
        Bindings b = new Bindings().addProtocol(StandardProtocolFamily.INET).addAddress(InetAddress.getByName(Config.DEFAULT.getTestIpV4Address()));

        KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
        keyPair1 = generator.genKeyPair();
        keyPair2 = generator.genKeyPair();

        // bootstrap peer
        peer1 = new PeerBuilderDHT(new PeerBuilder(keyPair1).ports(Config.DEFAULT.getTestPort()).bindings(b).start()).start();
        peer1.storageLayer().protection(
                StorageLayer.ProtectionEnable.NONE,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY
        );

        // connect to bootstrap peer
        peer2 = new PeerBuilderDHT(new PeerBuilder(keyPair2).masterPeer(peer1.peer()).start()).start();
        peer1.storageLayer().protection(
                StorageLayer.ProtectionEnable.NONE,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY,
                StorageLayer.ProtectionEnable.ALL,
                StorageLayer.ProtectionMode.MASTER_PUBLIC_KEY
        );

        InetAddress connectionAddress = Inet4Address.getByName(Config.DEFAULT.getTestIpV4Address());

        // Future Discover
        FutureDiscover futureDiscover = peer2.peer().discover().inetAddress(connectionAddress).ports(Config.DEFAULT.getTestPort()).start();
        futureDiscover.awaitUninterruptibly();

        // Future Bootstrap
        FutureBootstrap futureBootstrap = peer2.peer().bootstrap().inetAddress(connectionAddress).ports(Config.DEFAULT.getTestPort()).start();
        futureBootstrap.awaitUninterruptibly();

        if (futureBootstrap.isFailed()) {
            logger.error("Failed to bootstrap peers. Reason: " + futureBootstrap.failedReason());
        }

        dhtStorageAdapter1 = new DhtStorageAdapter(peer1, Config.DEFAULT.getLocationsLocationKey());
        dhtStorageAdapter2 = new DhtStorageAdapter(peer2, Config.DEFAULT.getLocationsLocationKey());

        locationManager1 = new LocationManager(dhtStorageAdapter1);
        locationManager2 = new LocationManager(dhtStorageAdapter2);

        user1 = new User("testUser", keyPair1.getPublic(), new ArrayList<>());
        user2 = new User("testUser2", keyPair2.getPublic(), new ArrayList<>());

    }

    @After
    public void after()
            throws InputOutputException {
        locationManager1.removeClientLocation(user1, l1);
        locationManager1.removeClientLocation(user1, l2);
    }

    @Test
    public void testAddLocation()
            throws InputOutputException {
        locationManager1.addClientLocation(user1, l1);

        List<ClientLocation> result = locationManager1.getClientLocations(user1);

        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));
    }

    @Test
    public void testRemoveLocation()
            throws InputOutputException {
        locationManager1.addClientLocation(user1, l1);

        List<ClientLocation> result = locationManager1.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        locationManager1.removeClientLocation(user1, l1);

        List<ClientLocation> retAfterDeletion = locationManager1.getClientLocations(user1);
        assertEquals("Result after deletion has different amount of locations saved", 0, retAfterDeletion.size());
    }

    @Test
    public void testGetLocations()
            throws InputOutputException {
        locationManager1.addClientLocation(user1, l1);
        locationManager1.addClientLocation(user1, l2);

        List<ClientLocation> result = locationManager1.getClientLocations(user1);
        assertEquals("Result has not both locations in it", 2, result.size());
        assertThat("Result does not contain both locations", result, hasItems(l1, l2));
    }

    @Test
    public void testAccessForOtherUsers()
            throws InputOutputException {
        locationManager1.addClientLocation(user1, l1);

        // location manager of user2 wants to receive locations
        // from user1
        List<ClientLocation> result = locationManager2.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 1, result.size());
        assertThat("result does not contain location", result, hasItem(l1));

        // user2 wants to persist a location for user1,
        // since he is not the correct owner, he can not persist anything
        // owner := hash(public_key) == domain
        // e.g Owner of domain 0x1234 is peer where 0x1234 == hash(public_key)
        locationManager2.addClientLocation(user1, l2);

        // TODO: currently, it is only working because user1 has already claimed the domain
        // TODO: required would be that no peer can store anything on another's domain even
        // TODO: if the domain owner did not claim the domain yet

        List<ClientLocation> resultAfterAdding = locationManager2.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 1, resultAfterAdding.size());
        assertThat("Result does not contain location l1", resultAfterAdding, hasItem(l1));
        assertThat("result should not contain location l2", resultAfterAdding, not(hasItem(l2)));


        locationManager1.addClientLocation(user1, l2);
        // we should be able to get all locations from another user
        List<ClientLocation> result2 = locationManager2.getClientLocations(user1);
        assertEquals("Result has different amount of locations saved", 2, result2.size());
        assertThat("Result does not contain location l1", result2, hasItem(l1));
        assertThat("result should not contain location l2", result2, hasItem(l1));
    }

}

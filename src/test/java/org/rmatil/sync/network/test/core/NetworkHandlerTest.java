package org.rmatil.sync.network.test.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.IClient;
import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.Client;
import org.rmatil.sync.network.core.ClientManager;
import org.rmatil.sync.network.core.messaging.ObjectDataReplyHandler;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.persistence.core.dht.DhtStorageAdapter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class NetworkHandlerTest {

    protected static IUser user;

    protected static IClientManager clientManager1;
    protected static IClientManager clientManager2;

    protected static IClient client1;
    protected static IClient client2;

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
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

        client1 = new Client(
                Config.IPv4,
                user,
                clientDeviceId1
        );

        client2 = new Client(
                Config.IPv4_2,
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

        DhtStorageAdapter dhtStorageAdapter1 = new DhtStorageAdapter(
                client1.getPeerDht()
        );

        clientManager1 = new ClientManager(
                dhtStorageAdapter1,
                Config.IPv4.getLocationsContentKey(),
                Config.IPv4.getPrivateKeyContentKey(),
                Config.IPv4.getPublicKeyContentKey(),
                Config.IPv4.getSaltContentKey(),
                Config.IPv4.getDomainKey()
        );

        DhtStorageAdapter dhtStorageAdapter2 = new DhtStorageAdapter(
                client2.getPeerDht()
        );

        clientManager2 = new ClientManager(
                dhtStorageAdapter2,
                Config.IPv4.getLocationsContentKey(),
                Config.IPv4.getPrivateKeyContentKey(),
                Config.IPv4.getPublicKeyContentKey(),
                Config.IPv4.getSaltContentKey(),
                Config.IPv4.getDomainKey()
        );
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

package org.rmatil.sync.network.test.core;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.Client;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.model.User;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class ClientTest {

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

        clientIpV6 = new Client(Config.IPv6, user, UUID.randomUUID());
        clientIpV4_1 = new Client(Config.IPv4, user, UUID.randomUUID());
        clientIpV4_2 = new Client(Config.IPv4_2, user2, UUID.randomUUID());
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
        boolean succeededV4_1 = clientIpV4_1.start();

        assertTrue("IPv4_1 client did not succeed to start", succeededV4_1);

        ClientLocation ipV4_1Client = new ClientLocation(UUID.randomUUID(), clientIpV4_1.getPeerAddress());

        boolean succeededV4 = clientIpV4_2.start(ipV4_1Client.getIpAddress(), ipV4_1Client.getPort());

        assertTrue("IPv4 2 client did not succeed to start", succeededV4);
    }

}

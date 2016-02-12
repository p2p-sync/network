package org.rmatil.sync.network.test.core.model;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.api.INode;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.Node;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.model.User;
import org.rmatil.sync.network.test.core.base.BaseTest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ClientLocationTest {

    protected static INode clientIpV4;
    protected static INode clientIpV6;

    protected static UUID clientId1 = UUID.randomUUID();
    protected static UUID clientId2 = UUID.randomUUID();

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("DSA");
        KeyPair keyPair = gen.genKeyPair();

        IUser user = new User("Weir Doe", "WillBorrow", "Bread", keyPair.getPublic(), keyPair.getPrivate(), new ArrayList<>());
        clientIpV4 = new Node(BaseTest.getTestConfig1(), user, clientId1);
        clientIpV6 = new Node(BaseTest.getTestConfig2(), user, clientId2);

        clientIpV4.start();
        clientIpV6.start();
    }

    @AfterClass
    public static void tearDown() {
        clientIpV4.shutdown();
        clientIpV6.shutdown();
    }


    @Test
    public void testClientLocation() {
        ClientLocation clientLocationIpV4 = new ClientLocation(
                clientId1,
                clientIpV4.getPeerAddress()
        );

        ClientLocation clientLocationIpV6 = new ClientLocation(
                clientId2,
                clientIpV6.getPeerAddress()
        );

        assertTrue("IPv4 client should have an IPv4 address", clientLocationIpV4.isIpV4());
        assertEquals("ClientPeerAddress should be equal", clientIpV4.getPeerAddress(), clientLocationIpV4.getPeerAddress());
        assertEquals("ClientId should be equal", clientIpV4.getClientDeviceId(), clientLocationIpV4.getClientDeviceId());

        assertThat("IPv4 address should not have a slash in it", clientLocationIpV4.getIpAddress(), not(containsString("/")));

        assertTrue("IPv6 client should have an IPv6 address", clientLocationIpV6.isIpV4());
        assertEquals("ClientPeerAddress should be equal", clientIpV6.getPeerAddress(), clientLocationIpV6.getPeerAddress());
        assertEquals("ClientId should be equal", clientIpV6.getClientDeviceId(), clientLocationIpV6.getClientDeviceId());

        assertThat("IPv6 address should not have a slash in it", clientLocationIpV6.getIpAddress(), not(containsString("%")));
    }
}

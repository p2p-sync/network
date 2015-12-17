package org.rmatil.sync.network.test.core.serialize;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.network.core.serialize.ByteSerializer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class ByteSerializerTest {

    protected static List<ClientLocation> locations = new ArrayList<>();

    protected static ClientLocation l1;
    protected static ClientLocation l2;

    @BeforeClass
    public static void before()
            throws UnknownHostException {
        PeerAddress peerAddress = new PeerAddress(Number160.ONE, InetAddress.getLocalHost());
        PeerAddress peerAddress2 = new PeerAddress(Number160.ZERO, InetAddress.getLocalHost());

        l1 = new ClientLocation(peerAddress);
        l2 = new ClientLocation(peerAddress2);

        locations.add(l1);
        locations.add(l2);
    }

    @Test
    public void testToBytes()
            throws IOException, ClassNotFoundException {
        byte[] bytes = ByteSerializer.toBytes(locations);

        List<ClientLocation> clientLocations = (List<ClientLocation>) ByteSerializer.fromBytes(bytes);

        assertThat("Client locations do not contain location1", clientLocations, hasItems(l1, l2));
    }

}

package org.rmatil.sync.network.test.core.serialize;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.serialize.ByteSerializer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class ByteSerializerTest {

    protected static List<NodeLocation> locations = new ArrayList<>();

    protected static NodeLocation l1;
    protected static NodeLocation l2;

    @BeforeClass
    public static void before()
            throws UnknownHostException {
        PeerAddress peerAddress = new PeerAddress(Number160.ONE, InetAddress.getLocalHost());
        PeerAddress peerAddress2 = new PeerAddress(Number160.ZERO, InetAddress.getLocalHost());

        l1 = new NodeLocation(UUID.randomUUID(), peerAddress);
        l2 = new NodeLocation(UUID.randomUUID(), peerAddress2);

        locations.add(l1);
        locations.add(l2);
    }

    @Test
    public void testToBytes()
            throws IOException, ClassNotFoundException {
        byte[] bytes = ByteSerializer.toBytes(locations);

        List<NodeLocation> nodeLocations = (List<NodeLocation>) ByteSerializer.fromBytes(bytes);

        assertThat("Node locations do not contain location1", nodeLocations, hasItems(l1, l2));
    }

}

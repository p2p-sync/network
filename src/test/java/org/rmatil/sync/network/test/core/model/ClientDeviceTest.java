package org.rmatil.sync.network.test.core.model;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import org.junit.Test;
import org.rmatil.sync.network.core.model.ClientDevice;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ClientDeviceTest {

    @Test
    public void testAccessor() {
        String userName = "Brandon Guidelines";
        UUID clientDeviceId = UUID.randomUUID();
        PeerAddress peerAddress = new PeerAddress(Number160.ZERO);
        PeerAddress peerAddress1 = new PeerAddress(Number160.ONE);

        ClientDevice clientDevice = new ClientDevice(
                userName,
                clientDeviceId,
                peerAddress
        );

        assertEquals("UserName is not equal", userName, clientDevice.getUserName());
        assertEquals("ClientDeviceId is not equal", clientDeviceId, clientDevice.getClientDeviceId());
        assertEquals("PeerAddress is not equal", peerAddress, clientDevice.getPeerAddress());

        clientDevice.setPeerAddress(peerAddress1);

        assertEquals("PeerAddress is not eqal after change", peerAddress1, clientDevice.getPeerAddress());

    }

}

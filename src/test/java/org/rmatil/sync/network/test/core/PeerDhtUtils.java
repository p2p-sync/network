package org.rmatil.sync.network.test.core;

import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.StandardProtocolFamily;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.config.Config;
import org.rmatil.sync.network.core.model.ClientLocation;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class PeerDhtUtils {

    protected static Random rnd = new Random(42L);

    public static PeerDHT initPeerDht(Config config, IUser user)
            throws IOException {
        Bindings bindings = new Bindings();
        if (config.useIpV6()) {
            bindings.addProtocol(StandardProtocolFamily.INET6);
            bindings.addAddress(Inet4Address.getLocalHost());
        } else {
            bindings.addProtocol(StandardProtocolFamily.INET);
            bindings.addAddress(Inet6Address.getLocalHost());
        }

        PeerDHT peerDht = new PeerBuilderDHT(
                new PeerBuilder(new Number160(rnd))
                        .keyPair(user.getKeyPair())
                        .ports(config.getPort())
                        .bindings(bindings)
                        .start()
        ).start();


        peerDht.storageLayer()
                .protection(
                        config.getProtectionDomainEnable(),
                        config.getProtectionDomainMode(),
                        config.getProtectionEntryEnable(),
                        config.getProtectionEntryMode()
                );

        return peerDht;
    }

    public static void connectToLocation(PeerDHT peerDht, ClientLocation bootstrapLocation)
            throws UnknownHostException {
        InetAddress address = InetAddress.getByName(bootstrapLocation.getIpAddress());
        FutureDiscover futureDiscover = peerDht
                .peer()
                .discover()
                .inetAddress(address)
                .ports(bootstrapLocation.getPort())
                .start();
        futureDiscover.awaitUninterruptibly();

        if (futureDiscover.isFailed()) {
            throw new RuntimeException("Can not discover other client at address " + bootstrapLocation.getIpAddress() + ":" + bootstrapLocation.getPort() + ". Message: " + futureDiscover.failedReason());
        }

        // if found, try to bootstrap to it
        FutureBootstrap futureBootstrap = peerDht
                .peer()
                .bootstrap()
                .inetAddress(address)
                .ports(bootstrapLocation.getPort())
                .start();
        futureBootstrap.awaitUninterruptibly();

        if (futureBootstrap.isFailed()) {
            throw new RuntimeException("Can not bootstrap to address " + bootstrapLocation.getIpAddress() + ". Message: " + futureBootstrap.failedReason());
        }
    }
}

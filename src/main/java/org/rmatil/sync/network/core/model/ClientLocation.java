package org.rmatil.sync.network.core.model;

import net.tomp2p.peers.PeerAddress;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.UUID;

/**
 * A model which represents a single location of a client
 * from a particular user
 */
public class ClientLocation implements Serializable {

    /**
     * The peer address
     */
    protected PeerAddress peerAddress;

    /**
     * The uuid of this client device
     */
    protected UUID clientDeviceId;

    /**
     * @param peerAddress The peer address to add
     */
    public ClientLocation(UUID clientDeviceId, PeerAddress peerAddress) {
        this.clientDeviceId = clientDeviceId;
        this.peerAddress = peerAddress;
    }

    /**
     * Returns true if the IP address is using
     * the v4-naming scheme. False otherwise
     *
     * @return True, if the client uses an IPv4 address to contact
     */
    public boolean isIpV4() {
        return this.peerAddress.isIPv4();
    }

    /**
     * Returns the IP address of the client
     *
     * @return The IP address
     */
    public String getIpAddress() {
        String ipAddress = "";
        // in case the ip is v6, then a scope id identifying the interface is returned in the host address.
        // since this information is not needed, we strip this out
        String ipWithInterfaceScopeId = this.peerAddress.inetAddress().getHostAddress();
        int idxIpV6 = ipWithInterfaceScopeId.indexOf("%");

        // there might be a leading slash before the ip address
        int idxIpV4 = ipWithInterfaceScopeId.indexOf("/");
        if (idxIpV6 > 0) {
            ipAddress = ipWithInterfaceScopeId.substring(0, idxIpV6);
        } else if (idxIpV4 > 0) {
            ipAddress = ipWithInterfaceScopeId.replace("/", "");
        } else {
            ipAddress = ipWithInterfaceScopeId;
        }

        return ipAddress;
    }


    /**
     * Returns the port of the client
     *
     * @return The port
     */
    public int getPort() {
        return this.peerAddress.tcpPort();
    }

    /**
     * Returns the UUID of this client device
     *
     * @return The uuid
     */
    public UUID getClientDeviceId() {
        return clientDeviceId;
    }

    /**
     * Returns the peer address
     *
     * @return The peer address
     */
    public PeerAddress getPeerAddress() {
        return peerAddress;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(this.getIpAddress()).
                        append(this.getPort()).
                        append(this.getClientDeviceId()).
                        toHashCode();
    }

    public boolean equals(Object obj) {
        if (! (obj instanceof ClientLocation)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        ClientLocation rhs = (ClientLocation) obj;
        return new EqualsBuilder()
                // if deriving: appendSuper(super.equals(obj)).
                .append(this.getIpAddress(), rhs.getIpAddress())
                .append(this.getPort(), rhs.getPort())
                .append(this.getClientDeviceId(), rhs.getClientDeviceId())
                .isEquals();
    }

}

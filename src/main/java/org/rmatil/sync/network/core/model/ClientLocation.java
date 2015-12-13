package org.rmatil.sync.network.core.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * A model which represents a single location of a client
 * from a particular user
 */
public class ClientLocation implements Serializable {

    /**
     * The ip address of the client
     */
    protected String ipAddress;

    /**
     * The port on which the client can be contacted
     */
    protected int port;

    /**
     * @param ipAddress The IP address of the client
     * @param port      The port of the client
     */
    public ClientLocation(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * Returns true if the IP address is using
     * the v4-naming scheme. False otherwise
     *
     * @return True, if the client uses an IPv4 address to contact
     */
    public boolean isIpV4() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Returns the IP address of the client
     *
     * @return The IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Returns the port of the client
     *
     * @return The port
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                // if deriving: appendSuper(super.hashCode()).
                        append(ipAddress).
                        append(port).
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
        return new EqualsBuilder().
                // if deriving: appendSuper(super.equals(obj)).
                        append(ipAddress, rhs.ipAddress).
                        append(port, rhs.port).
                        isEquals();
    }

}

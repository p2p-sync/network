package org.rmatil.sync.network.core;

/**
 * Holds common parameters to customize the node
 */
public class ConnectionConfiguration {

    /**
     * The id of the node
     */
    protected String nodeId;

    /**
     * The port on which this node should
     * be started, if available
     */
    protected int port;

    /**
     * The time to live for elements in
     * the DHT Cache (in milliseconds)
     */
    protected long cacheTtl;

    /**
     * The maximum timeout to wait for discovery
     * of another node (in milliseconds)
     */
    protected long peerDiscoveryTimeout;

    /**
     * The maximum timeout to wait for bootstrapping
     * to another node (in milliseconds)
     */
    protected long peerBootstrapTimeout;

    /**
     * The maximum timeout to wait for announcing
     * the shutdown of this node to other peers
     */
    protected long shutdownAnnounceTimeout;

    /**
     * Whether this peer is behind a firewall and
     * UPnP should be tried to use to establish a connection
     */
    protected boolean isFirewalled;


    /**
     * @param nodeId                  The id of the node
     * @param port                    The port on which this node should be started (if available)
     * @param cacheTtl                The time to live for elements in the DHT cache (in milliseconds)
     * @param peerDiscoveryTimeout    The maximum timeout for discovering another peer (in milliseconds)
     * @param peerBootstrapTimeout    The maximum timeout to wait for a bootstrap to another peer (in milliseconds)
     * @param shutdownAnnounceTimeout The maximum timeout to wait for a completed shutdown announce of this node (in milliseconds)
     * @param isFirewalled            Whether this peer is behind a firewall and UPnP should be used
     */
    public ConnectionConfiguration(String nodeId, int port, long cacheTtl, long peerDiscoveryTimeout, long peerBootstrapTimeout, long shutdownAnnounceTimeout, boolean isFirewalled) {
        this.nodeId = nodeId;
        this.port = port;
        this.cacheTtl = cacheTtl;
        this.peerDiscoveryTimeout = peerDiscoveryTimeout;
        this.peerBootstrapTimeout = peerBootstrapTimeout;
        this.shutdownAnnounceTimeout = shutdownAnnounceTimeout;
        this.isFirewalled = isFirewalled;
    }

    /**
     * Returns the if of this node
     *
     * @return The node id
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Returns the port on which this node should be started (if available)
     *
     * @return The port on which to start
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the time to live for elements in the DHT Cache (in milliseconds)
     *
     * @return The time to live (in milliseconds)
     */
    public long getCacheTtl() {
        return cacheTtl;
    }

    /**
     * Returns the maximum timeout to wait for a discovery of another peer (in milliseconds)
     *
     * @return The maximum timeout (in milliseconds)
     */
    public long getPeerDiscoveryTimeout() {
        return peerDiscoveryTimeout;
    }

    /**
     * Returns the maximum timeout to wait for a complete bootstrap to another peer (in milliseconds)
     *
     * @return The maximum timeout (in milliseconds)
     */
    public long getPeerBootstrapTimeout() {
        return peerBootstrapTimeout;
    }

    /**
     * Returns the maximum timeout to wait for a successfull shutdown of this node (in milliseconds)
     *
     * @return The maximum timeout (in milliseconds)
     */
    public long getShutdownAnnounceTimeout() {
        return shutdownAnnounceTimeout;
    }

    /**
     * Returns true, if this node is behind a firewall and UPnP should be tried to use
     *
     * @return True, if UPnP should be tried
     */
    public boolean isFirewalled() {
        return isFirewalled;
    }
}

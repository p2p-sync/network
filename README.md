# Network Component

[![Build Status](https://travis-ci.org/p2p-sync/network.svg)](https://travis-ci.org/p2p-sync/network)
[![Coverage Status](https://coveralls.io/repos/p2p-sync/network/badge.svg?branch=master&service=github)](https://coveralls.io/github/p2p-sync/network?branch=master)

# Install
Use Maven to install this component into your project

```xml

<repositories>
  <repository>
    <id>network-mvn-repo</id>
    <url>https://raw.github.com/p2p-sync/network/mvn-repo/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>org.rmatil.sync.network</groupId>
    <artifactId>sync-network</artifactId>
    <version>0.1-SNAPSHOT</version>
  </dependency>
</dependencies>

```

# Overview
This component consists of parts which handle operations in the p2p network.

## Location Manager
The location manager is responsible to save, fetch and alter client locations of a particular user. 
Currently, client locations are stored in a distributed hash table (DHT).
The location key in the DHT is the hash of the corresponding username, the content key is specified as `LOCATION` (see also [here](https://github.com/p2p-sync/network/blob/648462f0db1462a1a57d6cedb2122c7cb49e9177/src/main/java/org/rmatil/sync/network/config/Config.java#L5)). To restrict write access to locations, domain protection keys are used. As per definition, a domain is protected if its owner is the peer having the id match the hash of its public key. 

> Owner of domain 0x1234 is peer where 0x1234 == hash(public_key)
>
> -- <cite>Stiller, B., Bocek, T. et al. (2015). P2P with TomP2P</cite>
  

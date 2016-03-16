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
This component holds functionalities to create a P2P network and communication between the peers in the network.
Note, that this module depends on [TomP2P](https://github.com/tomp2p/TomP2P) to create the network and communication.

## Node
A node, specified by [`INode`](https://github.com/p2p-sync/network/blob/master/src/main/java/org/rmatil/sync/network/api/INode.java) represents a peer within the network. It can be started either as initial node or connected to another node already
running this component.

## Node Manager
Nodes can persist some information directly in the network, using a Distributed Hash Table. The interface  [`INodeManager`](https://github.com/p2p-sync/network/blob/master/src/main/java/org/rmatil/sync/network/api/INodeManager.java)
specifies which data can be stored:

* A list of locations of nodes joined in the network
* A private key of the user of the node
* A public key of the user of the node
* A salt of the user of the node


## Communication
To actually communicate between nodes, TomP2P specifies an interface - `ObjectDataReply` - which is responsible to invoke
the correct functionality for different object types received by a node. This component provides an [`ObjectDataReplyHandler`](https://github.com/p2p-sync/network/blob/master/src/main/java/org/rmatil/sync/network/core/messaging/ObjectDataReplyHandler.java)
to which [`RequestCallbacks`](https://github.com/p2p-sync/network/blob/master/src/main/java/org/rmatil/sync/network/api/IRequestCallback.java) can be registered.

Networkhandler then actually invoke the communication between the clients. They send a [`IRequest`](https://github.com/p2p-sync/network/blob/master/src/main/java/org/rmatil/sync/network/api/IRequest.java) to another peer which then checks in his `ObjectDataReplyHandler` for an appropriate callback. Once such a handler is found
for the incoming request, it is invoked in a new thread. A [`IResponse`](https://github.com/p2p-sync/network/blob/master/src/main/java/org/rmatil/sync/network/api/IResponse.java) is then sent back to the requesting client, which previously registered his `NetworkHandler`-instance as callback for any 
appropriate incoming response. 

For an detailed example, see the [`NetworkHandlerTest`](https://github.com/p2p-sync/network/blob/master/src/test/java/org/rmatil/sync/network/test/core/NetworkHandlerTest.java#L90)

# Usage 
The following snippet shows the basic functionality to start and connect to nodes.

```java
import net.tomp2p.peers.PeerAddress;
import org.rmatil.sync.network.api.INode;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.core.ConnectionConfiguration;
import org.rmatil.sync.network.core.Node;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.network.core.model.User;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// ...

  UUID nodeId = UUID.randomUUID();

  ConnectionConfiguration config = new ConnectionConfiguration(
    nodeId.toString(), // a random id for the node
    4003, // the port on which the node should be started
    1000L, // time to live for values stored in the distributed hash table cache (in ms)
    10000L, // how long the node should try to discover his bootstrap node (in ms)
    10000L, // how long the node should try to bootstrap to the bootstrap node
    5000L, // how long the node should wait until he successfully announced his shutdown to other peers (in ms)
    false // Whether the node is behind a firewall (is not implemented yet)
  );

  KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
  KeyPair keyPair = keyGen.genKeyPair();

  List<NodeLocation> nodeLocations = new ArrayList<>();

  IUser user = new User(
    "Piff Jenkins",
    "password",
    "salt",
    keyPair.getPublic(),
    keyPair.getPrivate(),
    nodeLocations
  );

  INode node = new Node(
    config,
    user,
    nodeId
  );
  
  // Set up the communication protocol
  ObjectDataReplyHandler replyHandler = new ObjectDataReplyHandler(node);
  replyHandler.addRequestCallbackHandler(SomeRequest.class, SomeRequestHandler.class);
  node.setObjectDataReplyHandler(replyHandler);

  // start the node as bootstrap peer
  node.start();

  // start the node and connect to another already started node
  node.start("192.168.1.45", 4003);
    
  // get the node's address
  PeerAddress address = node.getPeerAddress();
    
  // shut down the node
  node.shutdown();

```

# License

```

  Copyright 2015 rmatil

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  
```

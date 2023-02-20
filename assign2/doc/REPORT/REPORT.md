![Fig 0: FEUP logo](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled.png)

**PARALLEL AND DISTRIBUTED COMPUTATION**

Distributed and Partitioned Key-Value Store

Deborah Marques Lago - 201806102

Flávio Lobo Vaz - 201509918

José António Dantas Macedo - 201705226

Porto

2021/2022

---

**Table of contents**

![Fig 01: Table of contents](Untitled%20fd79196e765b49e68114d5c4ccf60a48/table_of_contents.jpg)

---

# Project main classes

## TestClient.java

This class is used to invoke any of the membership events (join or leave) as well as to invoke any of the operations on key-value pairs (put, get and delete) that will be interpreted by the Store.

The TestClient should be invoked as follows.

```bash
java main/TestClient <IP address> <port number> <operation> [<opnd>]
```

Example:

```bash
java main/TestClient 127.0.0.1 8000 put file.txt
```

In the case of a put, this class reads the file and sends the value of the file to the Store, the stores compute the key and return it to the client, in order for the client to be able to invoke *get key* and *delete key*.

The communication between clients and nodes is done using a TCP protocol.

![Fig.1: Client](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%201.png)

Fig.1: Client

## Store.java

This class represents a node.

Is used to interpret the client messages and perform the membership events and the key-value operations.

The Store should be invoked as follows.

```bash
java main/Store <IP_mcast_addr> <IP_mcast_port> <node_id> <Store_port>
```

Example:

```bash
java main/Store 224.0.0.1 6789 127.0.0.1 8000
```

When the Store is invoked, we open a thread for TCP connections and a thread for UDP connections, and create a folder with the name of the node ip address.

To ensure persistency, the data items and their keys must are stored in this folder.

This folder allows us to test if the cluster key-value operations are working as supposed besides the fact we are running all the nodes on the same machine. 

![Fig.2: Create a folder and open a thread for TCP](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%202.png)

Fig.2: Create a folder and open a thread for TCP

---

### TCP connection

The TCP is multi-threaded with pools, listening for a connection to be made to its socket, when the connection is accepted, a pool executes a handler to deal with the message.

This handler depending on the message received proceeds to perform the membership events and the key-value operations.

![Fig.3: TCP multi-thread with pools](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%203.png)

Fig.3: TCP multi-thread with pools

### UDP connection

UDP allows a node to send join or leave messages to other nodes in the cluster, in order to receive multicast messages from existing nodes in the cluster. A message is sent over UDP which is received in a multicast group. All nodes in the cluster are linked to this group.

It was implemented using thread pools, as we will see below, not only for the Handler that handles multicast messages, but we will also need a TCPServerMT as seen above, to handle the join and leave messages from the TestClient to the node that we want it to do some leave or join operation. But also another TCP channel with Thread to receive the response messages from cluster nodes.

Who sends message to the multicast group does not need to belong to it.

![Fig.4: UDP and multicast  multi-thread with pools](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%204.png)

Fig.4: UDP and multicast  multi-thread with pools

![Fig.5: TCP multi-thread which also participates in the UDP multicast connection](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%205.png)

Fig.5: TCP multi-thread which also participates in the UDP multicast connection

# **Membership Service**

The join and leave operation should be invoked as follows.

```
java main/TestClient 127.0.0.4 8000 join
```

```
java main/TestClient 127.0.0.4 8000 leave
```

**Implemented features:**

What determines that a node belongs to a cluster is the fact that **it is connected to a UDP multicast connection**, through which it receives join, leave requests or to **periodically** receive a membership message **to avoid failures every one second** as much as possible. This last part we have partially implemented.

Nodes, after receiving via TCP implemented with TheadPool, a message from the TestClient to join or leave, immediately get temporarily access to a TCP channel.
Counter values are incremented and stored correctly ( Some information has already been explained above.) .

Nodes, after receiving via TCP implemented with TheadPool, a message from the TestClient to join or leave, immediately get temporarily access to a TCP channel.
Counter values are incremented and stored correctly. With a preference always for the highest counter, as it indicates the most current operation.

![Fig.6: processCounter](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%206.png)

Fig.6: processCounter

**Messages:** 

We have membership message control, so that receiving 3 messages, the node immediately terminates the join or leave operations.

The event log, like where we have information about the cluster nodes, are implemented with structures that make sure we have only one event per node. Also, as a rule, events with the highest counter are stored. All this information is also stored in files. Being part of the cluster or leaving it respectively.

We implement the sending of messages, using objects of the Message class, which is serializable.

In a Message list, where each Message has a String like the format: idNode counter for the event logs and sha256(idNode) idNode. In the last two Messages, we have the JoinMS or leaveMS operation as a response to the nodes that want to Join or leave .

![Fig.7: Messages with objects](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%207.png)

Fig.7: Messages with objects

![Fig.8: non-volatile storage](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%208.png)

Fig.8: non-volatile storage

Each node that responds, before doing so, randomly waits between 2 to 1 second.

![Fig.9: Random time](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%209.png)

Fig.9: Random time

![Fig.10: Incomplete update of cluster nodes](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2010.png)

Fig.10: Incomplete update of cluster nodes

**Conclusion:**
In general, everything that was expected regarding the Membership Service was practically implemented. Being only incomplete, the periodic update request is limited to 32 event logs in the files.

# **Store Service**

The store service is responsible for handling the key-value operations (put, get and delete), and is implemented as a distributed partitioned hash table in which each cluster node stores the key-value pairs in a bucket.

To generate the keys we used SHA-256 for hashing the value, that way we don't have hash collisions.

In order to have a distributed partitioned hash table, we use consistent hashing to partition the key-value pairs among the nodes in the cluster.

![Fig.11: Consistent Hashing Function](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2011.png)

Fig.11: Consistent Hashing Function

Thus a client needs only know one of the cluster nodes to be able to access the key-value store.

When a node receives any of the key-value operations from the client we check the node from the cluster that has the closer key, if it is himself it proceeds to perform the key-value operation, if it is not him, the node sends the node responsible a message using TCP.

In case the responsible node is down, the message will be sent to the node ahead.

![Fig.12: Function that checks if the node is down, and gives the next node working](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2012.png)

Fig.12: Function that checks if the node is down, and gives the next node working

Handling the operations:

- **Put**
    - Saves the key-value pair in the node hash table
    - Creates a file, with the name as the key, and the content as the value
    - Saves that file in the node folder
    
    ![Fig.13: Put Function](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2013.png)
    
    Fig.13: Put Function
    
- **Get**
    - Get from the hash table the value correspondent to the key
    
    ![Fig.14: Get Function](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2014.png)
    
    Fig.14: Get Function
    
- **Delete**
    - Delete the key-value pair in the node hash table
    - Delete a file, with the name as the key
    
    ![Fig.15: Delete Function](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2015.png)
    
    Fig.15: Delete Function
    

Nodes may have to transfer keys to other nodes upon a membership change.

Handling the events:

- **Join event**
    
    When a node joins the cluster, it sends a message via TCP to the successor node asking for the keys that are smaller or equal to his id. 
    
    The successor of the node proceeds to check the keys he has, and if a key is smaller or equal to the joining node, it sends that key-value pair to him, via TCP through the put value function.
    
    ![Fig.16:  Function to ask the successor node for the keys](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2016.png)
    
    Fig.16:  Function to ask the successor node for the keys
    
    ![Fig.17 : Function that sends the key-value pair to the joining node](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2017.png)
    
    Fig.17 : Function that sends the key-value pair to the joining node
    
- **Leave event**
    
    Before leaving the cluster, the node iterate through his hashtable, sending the key-value pairs to the successor node and deleting key-value pairs.
    
    ![Fig.18 : Function that sends the key-value pair successor before leaving the cluster](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2018.png)
    
    Fig.18 : Function that sends the key-value pair successor before leaving the cluster
    

---

# **Fault-Tolerance**

On performing key-values operations, if the responsible node is down, the message will be sent to the node ahead,until there are no more nodes in the cluster.

Nodes that do not belong to the cluster, and we try to do leave, this operation is not allowed. Same for join when the node already belongs to the cluster.

Nodes that do not belong to the cluster, and we try to do leave, this operation is not allowed. Same for join when the node already belongs to the cluster. We also make sure that we keep event logs as up-to-date as possible for each node. If by chance in any join or leave operation, the event logs are updated, we always keep the id node with the corresponding highest counter of the operations.

**Example:**

![Fig.19: Function to get the next node up in the cluster](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2019.png)

Fig.19: Function to get the next node up in the cluster

![Fig.20: Fault-Tolerance Example](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2020.png)

Fig.20: Fault-Tolerance Example

---

# **Concurrency**

## **Thread-pools**

We use a TCP connection multi-threaded with pools, listening for a connection to be made to its socket, when the connection is accepted, a pool executes a handler to deal with the message.

We also use, somewhat in line with what was explained above about UDP, a mix of thread pools with simple multiThread. That deal with messages received by nodes via multicast. From the reply to these messages via TCP controlling the number of times, that the Membership Service is intended. In order not to clog the nodes that want to leave or join with messages.

A few more photos, which initially did not appear above:

![Fig.21: Extra SnapShot of the code](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2021.png)

Fig.21: Extra SnapShot of the code

![Fig.22: Extra SnapShot of the code](Untitled%20fd79196e765b49e68114d5c4ccf60a48/Untitled%2022.png)

Fig.22: Extra SnapShot of the code
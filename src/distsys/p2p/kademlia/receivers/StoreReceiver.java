package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.DistributedHashTable;
import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.messaging.StoreRequest;
import distsys.p2p.kademlia.messaging.StoreResponse;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.IOException;

public class StoreReceiver implements Receiver {
    private KademliaServer server;
    private Contact self;
    private RoutingTable routingTable;
    private DistributedHashTable hashTable;

    public StoreReceiver(KademliaServer server, Contact self, RoutingTable routingTable, DistributedHashTable hashTable) {
        this.server = server;
        this.self = self;
        this.routingTable = routingTable;
        this.hashTable = hashTable;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        StoreRequest message = (StoreRequest) incoming;
        Contact origin = message.getOrigin();
        routingTable.update(origin);
        System.out.println("Received store request");

        hashTable.store(message.getKey(), message.getContentToStore());

        Message response = new StoreResponse(self, true);
        server.reply(origin, response, communicationId);
    }

    @Override
    public void timeout(int communicationId) throws IOException {
        // TODO: Implement
    }
}

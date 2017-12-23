package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.DistributedHashTable;
import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.FindContentRequest;
import distsys.p2p.kademlia.messaging.FindContentResponse;
import distsys.p2p.kademlia.messaging.FindNodeResponse;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.IOException;
import java.util.List;

public class FindContentReceiver implements Receiver {

    private final KademliaServer server;
    private final Contact self;
    private final RoutingTable routingTable;
    private final DistributedHashTable hashTable;

    public FindContentReceiver(KademliaServer server, Contact self, RoutingTable routingTable, DistributedHashTable hashTable) {
        this.server = server;
        this.self = self;
        this.routingTable = routingTable;
        this.hashTable = hashTable;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        FindContentRequest message = (FindContentRequest) incoming;
        Contact origin = message.getOrigin();
        ID contentKey = message.getTarget();

        routingTable.update(origin);

        Message response;
        if (hashTable.contains(contentKey)) {
            System.out.println("I have the content!");
            response = new FindContentResponse(self, hashTable.retrieve(contentKey));
        } else {
            List<Contact> closest = routingTable.findClosest(contentKey, origin, 20);
            response = new FindNodeResponse(self, closest);
        }
        server.reply(origin, response, communicationId);
    }

    @Override
    public void timeout(int communicationId) throws IOException {

    }
}

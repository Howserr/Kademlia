package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.FindNodeRequest;
import distsys.p2p.kademlia.messaging.FindNodeResponse;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.IOException;
import java.util.List;

public class FindNodeReceiver implements Receiver {

    private final KademliaServer server;
    private final Contact self;
    private final RoutingTable routingTable;

    public FindNodeReceiver(KademliaServer server, Contact self, RoutingTable routingTable) {
        this.server = server;
        this.self = self;
        this.routingTable = routingTable;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        FindNodeRequest message = (FindNodeRequest) incoming;
        Contact origin = message.getOrigin();
        routingTable.update(origin);

        List<Contact> closest = routingTable.findClosest(message.getTarget(), origin,20);
        Message response = new FindNodeResponse(self, closest);
        server.reply(origin, response, communicationId);
    }

    @Override
    public void timeout(int communicationId) throws IOException {

    }
}

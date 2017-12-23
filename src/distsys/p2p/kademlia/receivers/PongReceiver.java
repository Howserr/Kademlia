package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.FindNodeResponse;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.messaging.PongMessage;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.IOException;

public class PongReceiver implements Receiver {

    private final RoutingTable routingTable;

    public PongReceiver(KademliaServer server, Contact self, RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        //System.out.println("Acknowledge receieved for communicationId: " + communicationId);
        if (!(incoming instanceof PongMessage)) {
            return;
        }

        PongMessage message = (PongMessage) incoming;
        routingTable.update(message.getOrigin());
    }

    @Override
    public void timeout(int communicationId) throws IOException {
        //System.out.println("Expected acknowledgement not received for communicationId: " + communicationId);
    }
}

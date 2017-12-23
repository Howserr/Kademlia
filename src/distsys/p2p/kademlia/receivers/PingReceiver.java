package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.messaging.PingMessage;
import distsys.p2p.kademlia.messaging.PongMessage;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.IOException;

public class PingReceiver implements Receiver {

    private final KademliaServer server;
    private final Contact self;
    private final RoutingTable routingTable;

    public PingReceiver(KademliaServer server, Contact self, RoutingTable routingTable) {
        this.server = server;
        this.self = self;
        this.routingTable = routingTable;
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        PingMessage message = (PingMessage) incoming;
        //System.out.println("Received connect from: " + message.getOrigin().id.toHexString());

        routingTable.update(message.getOrigin());

        PongMessage response = new PongMessage(self);

        server.reply(message.getOrigin(), response, communicationId);
    }

    @Override
    public void timeout(int communicationId) throws IOException {
    }
}

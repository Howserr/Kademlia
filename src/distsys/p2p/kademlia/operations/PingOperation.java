package distsys.p2p.kademlia.operations;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.messaging.PingMessage;
import distsys.p2p.kademlia.messaging.PongMessage;
import distsys.p2p.kademlia.receivers.Receiver;
import distsys.p2p.kademlia.routing.Contact;

import java.io.IOException;

public class PingOperation implements Operation, Receiver {
    private KademliaServer server;
    private Contact self;
    private Contact recipient;

    public PingOperation(KademliaServer server, Contact self, Contact recipient) {
        this.server = server;
        this.self = self;
        this.recipient = recipient;
    }

    @Override
    public void execute() throws IOException {
        PingMessage ping = new PingMessage(self);
        server.sendMessage(recipient, ping, this);
    }

    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        if (!(incoming instanceof PongMessage)) {
            return;
        }
        PongMessage message = (PongMessage) incoming;
        Contact origin = message.getOrigin();
        server.getRoutingTable().update(origin);
    }

    @Override
    public void timeout(int communicationId) throws IOException {
        server.getRoutingTable().setUnresponsiveContact(recipient);
    }
}

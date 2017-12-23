package distsys.p2p.kademlia.operations;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.messaging.*;
import distsys.p2p.kademlia.receivers.Receiver;
import distsys.p2p.kademlia.routing.Contact;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.List;

public class StoreOperation implements Operation, Receiver {

    private KademliaServer server;
    private Contact self;
    private String contentToStore;
    private ID contentKey;

    public StoreOperation(KademliaServer server, Contact self, String contentToStore) {
        this.server = server;
        this.self = self;
        this.contentToStore = contentToStore;
    }

    @Override
    public synchronized void execute() throws IOException {
        contentKey = new ID(DigestUtils.sha1Hex(contentToStore));
        NodeLookupOperation lookupOperation = new NodeLookupOperation(server, self, contentKey);
        lookupOperation.execute();
        List<Contact> contacts = lookupOperation.getClosestNodes();

        StoreRequest message = new StoreRequest(self, contentKey, contentToStore);

        int replicaCount = 15;
        int counter = 0;
        for (Contact contact : contacts) {
            if (counter >= replicaCount) {
                break;
            }
            this.server.sendMessage(contact, message, this);
            counter++;
        }
    }

    public ID getContentKey() {
        return contentKey;
    }



    @Override
    public synchronized void receive(Message incoming, int communicationId) throws IOException {
        if (!(incoming instanceof StoreResponse)) {
            return;
        }

        StoreResponse message = (StoreResponse) incoming;
        Contact origin = message.getOrigin();
        server.getRoutingTable().update(origin);
    }

    @Override
    public void timeout(int communicationId) throws IOException {

    }
}

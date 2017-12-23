package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.DistributedHashTable;
import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.receivers.*;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.DataInputStream;
import java.io.IOException;

public class MessageFactory {

    private final Contact self;
    private final RoutingTable routingTable;
    private final DistributedHashTable hashTable;

    public MessageFactory(Contact self, RoutingTable routingTable, DistributedHashTable hashTable)
    {
        this.self = self;
        this.routingTable = routingTable;
        this.hashTable = hashTable;
    }

    public Message createMessage(byte code, DataInputStream din) throws IOException {
        switch (code)
        {
            case PingMessage.code:
                return new PingMessage(din);
            case PongMessage.code:
                return new PongMessage(din);
            case FindNodeRequest.code:
                return new FindNodeRequest(din);
            case FindNodeResponse.code:
                return new FindNodeResponse(din);
            case StoreRequest.code:
                return new StoreRequest(din);
            case StoreResponse.code:
                return new StoreResponse(din);
            case FindContentRequest.code:
                return new FindContentRequest(din);
            case FindContentResponse.code:
                return new FindContentResponse(din);
            default:
                System.out.println(this.self + " - No Message handler found for message. Code: " + code);
                return new SimpleMessage(din);
        }
    }

    public Receiver createReceiver(byte code, KademliaServer server)
    {
        switch (code)
        {
            case PingMessage.code:
                return new PingReceiver(server, self, routingTable);
            case PongMessage.code:
                return new PongReceiver(server, self, routingTable);
            case FindNodeRequest.code:
                return new FindNodeReceiver(server, self, routingTable);
            case StoreRequest.code:
                return new StoreReceiver(server, self, routingTable, hashTable);
            case FindContentRequest.code:
                return new FindContentReceiver(server, self, routingTable, hashTable);
            default:
                System.out.println("No receiver found for message. Code: " + code);
                return new SimpleReceiver();
        }
    }
}

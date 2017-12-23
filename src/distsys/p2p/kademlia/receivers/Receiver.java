package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.messaging.Message;

import java.io.IOException;

public interface Receiver {
    void receive(Message incoming, int communicationId) throws IOException;
    void timeout(int communicationId) throws IOException;
}

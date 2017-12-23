package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.messaging.Message;

import java.io.IOException;

public class ContentReceiver implements Receiver {
    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        // TODO: implement
    }

    @Override
    public void timeout(int communicationId) throws IOException {
        // TODO: implement
    }
}

package distsys.p2p.kademlia.receivers;

import distsys.p2p.kademlia.messaging.Message;

import java.io.IOException;

public class SimpleReceiver implements Receiver {
    @Override
    public void receive(Message incoming, int communicationId) throws IOException {
        System.out.println("Received message: " + incoming);
    }

    @Override
    public void timeout(int communicationId) throws IOException {
        System.out.println("SimpleReceiver message timeout.");
    }
}

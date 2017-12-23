import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.NodeController;
import distsys.p2p.kademlia.routing.Contact;

import java.net.InetAddress;

public class BootstrapNode {
    public static void main(String[] args) {
        try {
            Contact self = new Contact(new ID("45a1991768206fd44604c6824e612876ef9e7c5b"), InetAddress.getLocalHost() , 1);
            NodeController node = new NodeController(self);
            node.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.NodeController;
import distsys.p2p.kademlia.routing.Contact;

import java.net.InetAddress;

public class TestNode {
    public static void main(String[] args) {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            Contact bootstrap = new Contact(new ID("45a1991768206fd44604c6824e612876ef9e7c5b"), localhost, 1);

            NodeController node = new NodeController(2);
            node.run();
            node.bootstrap();

            String content, result;
            ID contentKey;

            for (int i = 0; i < 1000; i++) {
                content = "test string " + i;
                contentKey = node.put(content);
                node.resetRoutingTable();
                result = node.get(contentKey);
                if (result.equals(content)) {
                    System.out.println("successfully retrieved stored content");
                } else {
                    System.out.println("utter failure");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

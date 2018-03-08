import distsys.p2p.kademlia.NodeController;

public class SwarmNodes {

    public static void main(String[] args) {
        try {
            for (int i = 0; i < 20000; i++) {
                try {
                    NodeController node = new NodeController(3 + i);
                    node.run();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

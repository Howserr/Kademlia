package distsys.p2p.kademlia.messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Streamable {
    void toStream(DataOutputStream out) throws IOException;
    void fromStream(DataInputStream in) throws IOException;
}

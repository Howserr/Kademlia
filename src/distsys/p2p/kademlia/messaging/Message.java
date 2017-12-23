package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.Contact;

public interface Message extends Streamable {
    byte getCode();
}

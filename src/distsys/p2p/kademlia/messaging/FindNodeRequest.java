package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FindNodeRequest implements Message {

    public static final byte code = 0x04;

    private Contact origin;
    private ID target;

    public FindNodeRequest(Contact origin, ID target) {
        this.origin = origin;
        this.target = target;
    }

    public FindNodeRequest(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    public Contact getOrigin() {
        return origin;
    }

    public ID getTarget() {
        return target;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        target.toStream(out);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        this.origin = new Contact(in);
        this.target = new ID(in);
    }
}

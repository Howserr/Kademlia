package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.ID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FindContentRequest implements Message {

    public static final byte code = 0x08;

    private Contact origin;
    private ID target;

    public FindContentRequest(Contact origin, ID target) {
        this.origin = origin;
        this.target = target;
    }

    public FindContentRequest(DataInputStream in) throws IOException {
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

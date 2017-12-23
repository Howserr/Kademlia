package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PongMessage implements Message {

    public static final byte code = 0x02;
    private Contact origin;

    public PongMessage(Contact origin) {
        this.origin = origin;
    }

    public PongMessage(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    public Contact getOrigin() {
        return origin;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        origin = new Contact(in);
    }
}

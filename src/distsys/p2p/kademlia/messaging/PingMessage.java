package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PingMessage implements Message {
    private Contact origin;
    public static final byte code = 0x03;

    public PingMessage(Contact origin) {
        this.origin = origin;
    }

    public PingMessage(DataInputStream in) throws IOException {
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
        this.origin = new Contact(in);
    }
}

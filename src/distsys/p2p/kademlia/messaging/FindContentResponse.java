package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FindContentResponse implements Message {
    public static final byte code = 0x09;

    private Contact origin;
    private String content;

    public FindContentResponse(Contact origin, String content) {
        this.origin = origin;
        this.content = content;
    }

    public FindContentResponse(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    public Contact getOrigin() {
        return origin;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        out.writeUTF(content);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        this.origin = new Contact(in);
        this.content = in.readUTF();
    }
}

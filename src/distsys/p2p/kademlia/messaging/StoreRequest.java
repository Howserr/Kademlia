package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StoreRequest implements Message {

    public static final byte code = 0x06;

    private Contact origin;
    private ID key;
    private String contentToStore;

    public StoreRequest(Contact origin, ID key, String contentToStore) {
        this.origin = origin;
        this.key = key;
        this.contentToStore = contentToStore;
    }

    public StoreRequest(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    public Contact getOrigin() {
        return origin;
    }

    public ID getKey() {
        return key;
    }

    public String getContentToStore() {
        return contentToStore;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        key.toStream(out);
        out.writeUTF(contentToStore);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        this.origin = new Contact(in);
        this.key = new ID(in);
        this.contentToStore = in.readUTF();
    }
}

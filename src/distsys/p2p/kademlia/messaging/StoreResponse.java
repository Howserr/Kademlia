package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StoreResponse implements Message {
    public static final byte code = 0x07;

    private Contact origin;
    private boolean storeSuccess;

    public StoreResponse(Contact origin, boolean storeSuccess) {
        this.origin = origin;
        this.storeSuccess = storeSuccess;
    }

    public StoreResponse(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    public Contact getOrigin() {
        return origin;
    }

    public boolean getStoreSuccess() {
        return storeSuccess;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        out.writeBoolean(storeSuccess);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        this.origin = new Contact(in);
        storeSuccess = in.readBoolean();
    }
}

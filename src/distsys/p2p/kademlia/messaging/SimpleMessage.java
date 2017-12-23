package distsys.p2p.kademlia.messaging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SimpleMessage implements Message {

    private static final int code = 0x01;

    private String content;

    public SimpleMessage(String message) {
        this.content = message;
    }

    public SimpleMessage(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        out.writeInt(content.length());
        out.writeBytes(content);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        byte[] buffer = new byte[in.readInt()];
        in.readFully(buffer);
        this.content = new String(buffer);
    }
}

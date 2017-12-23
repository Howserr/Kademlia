package distsys.p2p.kademlia.routing;

import distsys.p2p.kademlia.messaging.Streamable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Objects;

public class Contact implements Serializable, Streamable {
    public ID id;
    public InetAddress inetAddress;
    public int port;

    public Contact(ID id, InetAddress inetAddress, int port) {
        this.id = id;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public Contact(DataInputStream in) throws IOException {
        fromStream(in);
    }

    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(this.inetAddress, this.port);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Contact.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Contact other = (Contact) obj;
        if (!id.equals(other.id)) {
            return false;
        }
        if (!inetAddress.equals(other.inetAddress)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inetAddress, port);
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        id.toStream(out);
        byte[] address = inetAddress.getAddress();
        out.write(address);
        out.writeInt(port);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        id = new ID(in);

        byte[] address = new byte[4];
        in.readFully(address);
        inetAddress = InetAddress.getByAddress(address);
        port = in.readInt();
    }
}

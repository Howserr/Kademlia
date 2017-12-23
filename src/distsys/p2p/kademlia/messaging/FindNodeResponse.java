package distsys.p2p.kademlia.messaging;

import distsys.p2p.kademlia.routing.Contact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FindNodeResponse implements Message {
    public static final byte code = 0x05;

    private Contact origin;
    private List<Contact> contacts;

    public FindNodeResponse(Contact origin, List<Contact> contacts) {
        this.origin = origin;
        this.contacts = contacts;
    }

    public FindNodeResponse(DataInputStream in) throws IOException {
        fromStream(in);
    }

    @Override
    public byte getCode() {
        return code;
    }

    public Contact getOrigin() {
        return origin;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        origin.toStream(out);
        int length = this.contacts.size();
        out.writeInt(length);
        for (Contact contact: contacts) {
            contact.toStream(out);
        }
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        this.origin = new Contact(in);

        int length = in.readInt();
        this.contacts = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            this.contacts.add(new Contact(in));
        }
    }
}

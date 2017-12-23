package distsys.p2p.kademlia.routing;

import java.util.*;

public class Bucket {
    private short bucketSize;
    private List<Contact> contacts;

    public Bucket(short size) {
        this.bucketSize = size;
        contacts = Collections.synchronizedList(new ArrayList<>(bucketSize));
    }

    public Contact get(int index) {
        synchronized (contacts) {
            return contacts.get(index);
        }
    }

    public boolean add(Contact contact) {
        synchronized (contacts) {
            if (contacts.contains(contact)) {
                contacts.remove(contact);
                contacts.add(contact);
            } else {
                if (contacts.size() < bucketSize) {
                    contacts.add(contact);
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    public void remove(Contact contact) {
        synchronized (contacts) {
            if (contacts.contains(contact)) {
                contacts.remove(contact);
            }
        }
    }

    public int size() {
        synchronized (contacts) {
            return contacts.size();
        }
    }

    public List<Contact> getAll() {
        List<Contact> results = new LinkedList<>();
        synchronized (contacts) {
            Iterator i = contacts.iterator();
            while (i.hasNext()) {
                results.add((Contact) i.next());
            }
        }
        return results;
    }
}

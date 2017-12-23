package distsys.p2p.kademlia.routing;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.operations.PingOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class RoutingTable {
    private static final short BUCKET_SIZE = 20;
    private static final short ID_LENGTH = 20;

    private Contact self;
    Bucket[] buckets;

    private KademliaServer server;

    public RoutingTable(Contact self) {
        this.self = self;
        buckets = new Bucket[ID_LENGTH * 8];
        for(int i = 0; i < ID_LENGTH * 8; i++) {
            buckets[i] = new Bucket(BUCKET_SIZE);
        }
    }

    public void setNodeContact(Contact self) {
        this.self = self;
    }

    public void setServer(KademliaServer server) {
        this.server = server;
    }

    public List<Contact> getAll() {
        List<Contact> contacts = new ArrayList<>();

        for (Bucket bucket: buckets) {
            for (Contact contact: bucket.getAll()) {
                contacts.add(contact);
            }
        }
        return contacts;
    }

    public void update(Contact contact) {
        if (contact.id.equals(self.id)) {
            return;
        }
        int prefixLength = contact.id.xor(self.id).prefixLength();

        if (!buckets[prefixLength].add(contact)) {
//            Contact contactToCheck;
//            int bucketSize = buckets[prefixLength].size();
//            if (server != null) {
//                for (int i = 0; i < bucketSize; i++) {
//                    contactToCheck = buckets[prefixLength].get(i);
//                    PingOperation pingOperation = new PingOperation(server, self, contactToCheck);
//                    try {
//                        pingOperation.execute();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                buckets[prefixLength].add(contact);
//            }
        }
        //System.out.println("Contact: " + contact.id.toHexString() + " added to routing table");
    }

    public List<Contact> findClosest(ID target, Contact origin, int count) {
        TreeSet<Contact> sortedSet = new TreeSet<>(new ContactDistanceComparator(target));
        sortedSet.addAll(getAll());
        sortedSet.remove(origin);
        List<Contact> closest = new ArrayList<>(count);

        int i = 0;
        for (Contact contact: sortedSet) {
            closest.add(contact);
            if (++i == count) {
                break;
            }
        }
        return closest;
    }

    public void setUnresponsiveContacts(List<Contact> failedNodes) {
        for (Contact contact: failedNodes) {
            setUnresponsiveContact(contact);
        }
    }

    public synchronized void setUnresponsiveContact(Contact contact)
    {
        int prefixLength = contact.id.xor(self.id).prefixLength();
        this.buckets[prefixLength].remove(contact);
    }
}

package distsys.p2p.kademlia.operations;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.messaging.FindNodeRequest;
import distsys.p2p.kademlia.messaging.FindNodeResponse;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.receivers.Receiver;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.ContactDistanceComparator;

import java.io.IOException;
import java.util.*;

public class NodeLookupOperation implements Operation, Receiver {
    private int maxRuntime = 3000000;

    private final KademliaServer server;
    private final Contact self;
    private final Message findNodeRequest;
    private final TreeMap<Contact, ContactStatus> contacts;
    private final Map<Integer, Contact> communicationsAwaiting;
    private final ContactDistanceComparator comparator;

    public NodeLookupOperation(KademliaServer server, Contact self, ID target) {
        this.server = server;
        this.self = self;
        this.communicationsAwaiting = new HashMap<>();
        this.findNodeRequest = new FindNodeRequest(self, target);
        this.comparator = new ContactDistanceComparator(target);
        this.contacts = new TreeMap<>(this.comparator);
    }

    @Override
    public synchronized void execute() throws IOException {
        try {
            // So as not to cause a socket exception if own contact is returned and slips through
            contacts.put(this.self, ContactStatus.CONTACTED);
            this.addContacts(this.server.getRoutingTable().getAll());

            int totalTime = 0;
            int timeInterval = 10;
            while (totalTime < this.maxRuntime) {
                if (!this.processMessages()) {
                    wait(timeInterval);
                    totalTime += timeInterval;
                }
                else {
                    break;
                }
            }
            this.server.getRoutingTable().setUnresponsiveContacts(this.getFailedNodes());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Contact> getClosestNodes() {
        return this.getClosestContacts(ContactStatus.CONTACTED);
    }

    public void addContacts(List<Contact> contacts) {
        for (Contact contact : contacts) {
            if (!this.contacts.containsKey(contact)) {
                this.contacts.put(contact, ContactStatus.UNCONTACTED);
            }
        }
    }

    private boolean processMessages() throws IOException {

        if (getClosestContacts(ContactStatus.CONTACTED).size() > 20) {
            return true;
        }

        if (this.communicationsAwaiting.size() > 1) {
            return false;
        }

        List<Contact> unasked = this.getClosestNotFailed(ContactStatus.UNCONTACTED);
        if (unasked.isEmpty() && this.communicationsAwaiting.isEmpty()) {
            return true;
        }

        for (int i = 0; (this.communicationsAwaiting.size() < 1) && (i < unasked.size()); i++) {
            Contact contact = unasked.get(i);
            int comm = server.sendMessage(contact, findNodeRequest, this);
            this.contacts.put(contact, ContactStatus.AWAITING);
            this.communicationsAwaiting.put(comm, contact);
        }
        return false;
    }

    private List<Contact> getClosestContacts(ContactStatus status) {
        List<Contact> closestContacts = new ArrayList<>(20);
        int remainingSpaces = 20;
        for (Map.Entry e : this.contacts.entrySet()) {
            if (status.equals(e.getValue())) {
                closestContacts.add((Contact) e.getKey());
                if (--remainingSpaces == 0) {
                    break;
                }
            }
        }
        return closestContacts;
    }

    private List<Contact> getClosestNotFailed(ContactStatus status) {
        List<Contact> closestContacts = new ArrayList<>(20);
        int remainingSpaces = 20;
        for (Map.Entry<Contact, ContactStatus> e : this.contacts.entrySet()) {
            if (!ContactStatus.FAILED.equals(e.getValue())) {
                if (status.equals(e.getValue())) {
                    closestContacts.add(e.getKey());
                }
                if (--remainingSpaces == 0) {
                    break;
                }
            }
        }
        return closestContacts;
    }

    @Override
    public synchronized void receive(Message incoming, int communicationId) throws IOException {
        if (!(incoming instanceof FindNodeResponse)) {
            return;
        }
        FindNodeResponse message = (FindNodeResponse) incoming;

        Contact origin = message.getOrigin();
        this.server.getRoutingTable().update(origin);

        this.contacts.put(origin, ContactStatus.CONTACTED);

        this.communicationsAwaiting.remove(communicationId);

        this.addContacts(message.getContacts());
        this.processMessages();
    }

    @Override
    public synchronized void timeout(int communicationId) throws IOException {
        Contact contact = this.communicationsAwaiting.get(communicationId);
        if (contact == null) {
            return;
        }

        this.contacts.put(contact, ContactStatus.FAILED);
        this.server.getRoutingTable().setUnresponsiveContact(contact);
        this.communicationsAwaiting.remove(communicationId);

        this.processMessages();
    }

    public List<Contact> getFailedNodes() {
        List<Contact> failedNodes = new ArrayList<>();

        for (Map.Entry<Contact, ContactStatus> e : this.contacts.entrySet()) {
            if (e.getValue().equals(ContactStatus.FAILED)) {
                failedNodes.add(e.getKey());
            }
        }

        return failedNodes;
    }
}

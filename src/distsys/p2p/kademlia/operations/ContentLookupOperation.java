package distsys.p2p.kademlia.operations;

import distsys.p2p.kademlia.KademliaServer;
import distsys.p2p.kademlia.messaging.FindContentRequest;
import distsys.p2p.kademlia.messaging.FindContentResponse;
import distsys.p2p.kademlia.messaging.FindNodeResponse;
import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.receivers.Receiver;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.ContactDistanceComparator;
import distsys.p2p.kademlia.routing.ID;

import java.io.IOException;
import java.util.*;

public class ContentLookupOperation implements Operation, Receiver {
    private int maxRuntime = 10000;

    private final KademliaServer server;
    private final Contact self;
    private final Message findContentRequest;
    private final TreeMap<Contact, ContactStatus> contacts;
    private final Map<Integer, Contact> communicationsAwaiting;
    private final ContactDistanceComparator comparator;
    private String content;

    private boolean running;
    private int numberOfFindNodesCompleted;

    public ContentLookupOperation(KademliaServer server, Contact self, ID target) {
        this.server = server;
        this.self = self;
        this.communicationsAwaiting = new HashMap<>();
        this.findContentRequest = new FindContentRequest(self, target);
        this.comparator = new ContactDistanceComparator(target);
        this.contacts = new TreeMap<>(this.comparator);
        this.content = "";
    }

    @Override
    public synchronized void execute() throws IOException {
        try {
            this.running = true;
            // So as not to cause a socket exception if own contact is returned and slips through
            contacts.put(this.self, ContactStatus.CONTACTED);
            this.addContacts(this.server.getRoutingTable().getAll());

            int totalTimeWaited = 0;
            int timeInterval = 10;
            while (totalTimeWaited < this.maxRuntime) {
                if (!this.processMessaging()) {
                    wait(timeInterval);
                    totalTimeWaited += timeInterval;
                } else {
                    break;
                }
            }
            this.running = false;
            this.server.getRoutingTable().setUnresponsiveContacts(this.getFailedContacts());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getContent() {
        return this.content;
    }

    public int getNumberOfFindNodesCompleted() {
        return this.numberOfFindNodesCompleted;
    }

    public void addContacts(List<Contact> contacts) {
        for (Contact contact : contacts) {
            if (!this.contacts.containsKey(contact)) {
                this.contacts.put(contact, ContactStatus.UNCONTACTED);
            }
        }
    }

    private List<Contact> closestNodes(ContactStatus status) {
        List<Contact> closestNodes = new ArrayList<>(20);
        int remainingSpaces = 20;
        for (Map.Entry e : this.contacts.entrySet()) {
            if (status.equals(e.getValue())) {
                closestNodes.add((Contact) e.getKey());
                if (--remainingSpaces == 0) {
                    break;
                }
            }
        }
        return closestNodes;
    }

    private List<Contact> closestNodesNotFailed(ContactStatus status) {
        List<Contact> closestNodes = new ArrayList<>(20);
        int remainingSpaces = 20;
        for (Map.Entry<Contact, ContactStatus> e : this.contacts.entrySet()) {
            if (!ContactStatus.FAILED.equals(e.getValue())) {
                if (status.equals(e.getValue())) {
                    closestNodes.add(e.getKey());
                }
                if (--remainingSpaces == 0) {
                    break;
                }
            }
        }
        return closestNodes;
    }

    private boolean processMessaging() throws IOException {
        if (content != "") {
            // TODO: store content at the closest node that didn't have it
            return true;
        }
        if (3 <= this.communicationsAwaiting.size()) {
            return false;
        }
        List<Contact> unasked = this.closestNodesNotFailed(ContactStatus.UNCONTACTED);
        if (unasked.isEmpty() && this.communicationsAwaiting.isEmpty()) {
            return true;
        }
        for (int i = 0; (this.communicationsAwaiting.size() < 3) && (i < unasked.size()); i++) {
            Contact contact = unasked.get(i);
            int comm = server.sendMessage(contact, findContentRequest, this);
            this.contacts.put(contact, ContactStatus.AWAITING);
            this.communicationsAwaiting.put(comm, contact);
        }
        return false;
    }

    public List<Contact> getFailedContacts() {
        List<Contact> failedContacts = new ArrayList<>();

        for (Map.Entry<Contact, ContactStatus> e : this.contacts.entrySet()) {
            if (e.getValue().equals(ContactStatus.FAILED)) {
                failedContacts.add(e.getKey());
            }
        }

        return failedContacts;
    }

    @Override
    public synchronized void receive(Message incoming, int communicationId) throws IOException {
        if (!(incoming instanceof FindContentResponse) && !(incoming instanceof FindNodeResponse)) {
            return;
        }
        if (incoming instanceof FindContentResponse) {
            FindContentResponse message = (FindContentResponse) incoming;
            Contact origin = message.getOrigin();
            this.server.getRoutingTable().update(origin);
            this.contacts.put(origin, ContactStatus.CONTACTED);
            this.content = message.getContent();

            if (this.running) {
                this.numberOfFindNodesCompleted++;
            }
        }
        if (incoming instanceof FindNodeResponse) {
            System.out.println("received find node response to content lookup");
            FindNodeResponse message = (FindNodeResponse) incoming;
            Contact origin = message.getOrigin();
            this.server.getRoutingTable().update(origin);
            this.contacts.put(origin, ContactStatus.CONTACTED);
            this.addContacts(message.getContacts());

            if (this.running) {
                this.numberOfFindNodesCompleted++;
            }
        }
        this.communicationsAwaiting.remove(communicationId);
        this.processMessaging();
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

        this.processMessaging();
    }
}

package distsys.p2p.kademlia;

import distsys.p2p.kademlia.messaging.MessageFactory;
import distsys.p2p.kademlia.messaging.PingMessage;
import distsys.p2p.kademlia.operations.ContentLookupOperation;
import distsys.p2p.kademlia.operations.NodeLookupOperation;
import distsys.p2p.kademlia.operations.StoreOperation;
import distsys.p2p.kademlia.receivers.PongReceiver;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.ID;
import distsys.p2p.kademlia.routing.RoutingTable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeController implements Runnable {
    Contact self;
    KademliaServer server;
    RoutingTable routingTable;
    DistributedHashTable hashTable;
    private StopWatch stopwatch;
    private PrintWriter printWriter;

    private String testFileName = "speedtest-15-15000.txt";

    public NodeController(int port) {
        try {
            InetAddress host = InetAddress.getLocalHost();
            this.self = new Contact(new ID(), host, port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not get local host address", e);
        }
    }

    public NodeController(InetAddress host, int port) {
        this.self = new Contact(new ID(DigestUtils.sha1Hex(host + ":" + port)), host, port);
    }

    public NodeController(Contact self) {
        this.self = self;
    }

    public void run() {
        this.routingTable = new RoutingTable(self);
        this.hashTable = new DistributedHashTable(self);
        MessageFactory messageFactory = new MessageFactory(self, routingTable, hashTable);
        try {
            this.server = new KademliaServer(self, routingTable, messageFactory);
            routingTable.setServer(server);
            server.startListening();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        try {
            bootstrap();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ID put(String content) {
        this.stopwatch = new StopWatch();

        stopwatch.start();
        StoreOperation storeOperation = new StoreOperation(server, self, content);
        try {
            storeOperation.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopwatch.stop();
        long time = stopwatch.getNanoTime();
        try {
            this.printWriter = new PrintWriter(new FileWriter(testFileName, true));
            printWriter.println("put," + time);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopwatch.reset();
        return storeOperation.getContentKey();
    }

    public String get(ID key) {
        this.stopwatch = new StopWatch();
        stopwatch.start();
        ContentLookupOperation lookupOperation = new ContentLookupOperation(server, self, key);
        String result = "";
        try {
            lookupOperation.execute();
            result = lookupOperation.getContent();
            if (result != "") {
                System.out.println("Received content: " + lookupOperation.getContent());
            } else {
                System.out.println("Content for key not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopwatch.stop();
        long time = stopwatch.getNanoTime();
        System.out.println("Get operation execution time: " + time + "nano seconds");
        boolean success = "" != result;
        try {
            this.printWriter = new PrintWriter(new FileWriter(testFileName, true));
            printWriter.println("get," + time + "," + success);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        printWriter.close();
        stopwatch.reset();
        return result;
    }

    public void resetRoutingTable() throws IOException {
        this.routingTable = new RoutingTable(self);
        this.routingTable.setServer(server);
        System.out.println("reset routing table");
        bootstrap();
    }

    public synchronized void bootstrap() throws IOException {
        Contact bootstrap = new Contact(new ID("45a1991768206fd44604c6824e612876ef9e7c5b"), self.inetAddress, 1);
        PingMessage pingMessage = new PingMessage(self);
        PongReceiver pongReceiver = new PongReceiver(server, self, routingTable);
        server.sendMessage(bootstrap, pingMessage, pongReceiver);
        try {
            wait(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NodeLookupOperation lookupOperation = new NodeLookupOperation(server, self, self.id);
        lookupOperation.execute();
    }
}

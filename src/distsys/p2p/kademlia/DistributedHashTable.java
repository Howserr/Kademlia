package distsys.p2p.kademlia;

import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.ID;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Hashtable;

public class DistributedHashTable {
    private final Hashtable<ID, String> hashtable;
    private final Contact self;
    private PrintWriter printWriter;

    private String testFileName = "store-log.txt";

    public DistributedHashTable(Contact self) {
        this.hashtable = new Hashtable<>();
        this.self = self;
    }

    public boolean contains(ID key) {
        if (hashtable.containsKey(key)) {
            return true;
        }
        return false;
    }

    public boolean store(ID key, String value) {
        this.hashtable.put(key, value);
        try {
            this.printWriter = new PrintWriter(new FileWriter(testFileName, true));
            printWriter.println(self.id.toHexString() + "," + value);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String retrieve(ID key) {
        String result = "";
        if (hashtable.containsKey(key)) {
            result = hashtable.get(key);
        }
        return result;
    }
}

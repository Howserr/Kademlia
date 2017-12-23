package distsys.p2p.kademlia.routing;

import java.util.Comparator;

public class ContactDistanceComparator implements Comparator<Contact> {
    private final ID target;

    public ContactDistanceComparator(ID target) {
        this.target = target;
    }

    @Override
    public int compare(Contact c1, Contact c2) {
        ID xor1 = c1.id.xor(target);
        ID xor2 = c2.id.xor(target);

        return xor1.compareTo(xor2);
    }
}

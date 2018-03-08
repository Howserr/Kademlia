package distsys.p2p.kademlia.routing;

import java.math.BigInteger;
import java.util.Comparator;

public class ContactDistanceComparator implements Comparator<Contact> {
    private final ID target;

    public ContactDistanceComparator(ID target) {
        this.target = target;
    }

    @Override
    public int compare(Contact contact1, Contact contact2) {
        BigInteger xor1 = contact1.id.xor(target).getBigInt();
        BigInteger xor2 = contact2.id.xor(target).getBigInt();

        return xor1.compareTo(xor2);
    }
}

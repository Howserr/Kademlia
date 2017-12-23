package distsys.p2p.kademlia.routing;

import distsys.p2p.kademlia.messaging.Streamable;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;

public class ID implements Comparable<ID>, Serializable, Streamable {
    private static final short IDLENGTH = 20;

    byte[] id;

    public ID() {
        id = generateRandomID();
    }

    public ID(DataInputStream in) throws IOException {
        this.fromStream(in);
    }

    public ID(String hexBinary) {
        try {
            byte[] decoded = Hex.decodeHex(hexBinary);
            id = new byte[IDLENGTH];
            for (int i = 0; i < IDLENGTH; i++) {
                id[i] = decoded[i];
            }
        } catch (DecoderException e) {
            e.printStackTrace();
        }
    }

    public ID(byte[] value) {
        this.id = DigestUtils.sha1(value);
    }

    private byte[] generateRandomID() {
        byte[] randomId = new byte[IDLENGTH];
        Random random = new Random(LocalTime.now().getNano());
        for (int i = 0; i < IDLENGTH; i++) {
            int randomInt = random.nextInt(256);
            String hex = String.format("%02x", randomInt);
            try {
                randomId[i] = Hex.decodeHex(hex)[0];
            } catch (DecoderException e) {
                e.printStackTrace();
            }
        }
        return randomId;
    }

    public String toHexString() {
        return Hex.encodeHexString(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!ID.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final ID other = (ID) obj;
        if (!Arrays.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    boolean lessThan(ID other) {
        for (int i = 0; i < IDLENGTH; i++) {
            if (id[i] != other.id[i]) {
                return id[i] < other.id[i];
            }
        }
        return false;
    }

    public ID xor(ID other) {
        byte[] xor = new byte[IDLENGTH];
        for (int i = 0; i < IDLENGTH; i++) {
            xor[i] = (byte) (id[i] ^ other.id[i]);
        }
        return new ID(Hex.encodeHexString(xor));
    }

    public int prefixLength() {

        for (int i = 0; i < IDLENGTH; i++) {
            for (int j = 0; j < 8; j++) {
                if ((id[i] & (1 << j)) != 0) {
                    return i * 8 + j;
                }
            }
        }
        return IDLENGTH * 8 - 1;
    }

    @Override
    public int compareTo(ID other) {
        for (int i = 0; i < IDLENGTH; i++) {
            if (id[i] != other.id[i]) {
                String s1 = String.format("%8s", Integer.toBinaryString(id[i] & 0xFF)).replace(' ', '0');
                String s2 = String.format("%8s", Integer.toBinaryString(other.id[i] & 0xFF)).replace(' ', '0');
                return s1.compareTo(s2);
            }
        }
        return 0;
    }

    @Override
    public void toStream(DataOutputStream out) throws IOException {
        out.write(id);
    }

    @Override
    public void fromStream(DataInputStream in) throws IOException {
        byte[] buffer = new byte[IDLENGTH];
        in.readFully(buffer);
        this.id = buffer;
    }
}

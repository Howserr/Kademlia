package distsys.p2p.kademlia;

import distsys.p2p.kademlia.messaging.Message;
import distsys.p2p.kademlia.messaging.MessageFactory;
import distsys.p2p.kademlia.receivers.Receiver;
import distsys.p2p.kademlia.routing.Contact;
import distsys.p2p.kademlia.routing.RoutingTable;

import java.io.*;
import java.net.*;
import java.util.*;

public class KademliaServer {

    private static final int datagramSize = 64 * 1024;
    private static final int timeoutLength = 300000;

    private final DatagramSocket socket;
    private Contact self;
    private RoutingTable routingTable;
    private MessageFactory messageFactory;
    private Map<Integer, Receiver> receivers;
    private Map<Integer, TimerTask> tasks;
    private final Timer timer;

    public KademliaServer(Contact self, RoutingTable routingTable, MessageFactory messageFactory) throws SocketException, UnknownHostException {
        this.self = self;
        this.routingTable = routingTable;
        this.messageFactory = messageFactory;
        this.socket = new DatagramSocket(self.port);
        this.timer = new Timer(true);
        this.receivers = new HashMap<>();
        this.tasks = new HashMap<>();
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void startListening() {
        new Thread(() -> listen()).start();
        System.out.println("Now listening on port: " + self.port);
    }

    public synchronized int sendMessage(Contact recipient, Message message, Receiver receiver) throws IOException {
        int communicationId = new Random().nextInt();
        if (receiver != null) {
            try {

                receivers.put(communicationId, receiver);
                TimerTask task = new TimeoutTask(communicationId, receiver);
                timer.schedule(task, timeoutLength);
                tasks.put(communicationId, task);
            }
            catch (IllegalStateException ex) {

            }
        }
        sendMessage(recipient, message, communicationId);
        return communicationId;
    }

    public synchronized void reply(Contact recipient, Message msg, int communicationId) throws IOException {
        sendMessage(recipient, msg, communicationId);
    }

    private void sendMessage(Contact receipient, Message message, int communicationId) throws IOException {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(); DataOutputStream dataOut = new DataOutputStream(byteOut);) {
            dataOut.writeInt(communicationId);
            dataOut.writeByte(message.getCode());
            message.toStream(dataOut);
            dataOut.close();

            byte[] data = byteOut.toByteArray();

            if (data.length > datagramSize)
            {
                throw new IOException("Message is larger than the maximum datagram size");
            }

            DatagramPacket pkt = new DatagramPacket(data, 0, data.length);
            pkt.setSocketAddress(receipient.getSocketAddress());
            socket.send(pkt);
        }
    }

    private void listen() {
        while(true) {
            try {
                byte[] buffer = new byte[datagramSize];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                     DataInputStream dataIn = new DataInputStream(byteIn);) {

                    int communicationId = dataIn.readInt();
                    byte messageCode = dataIn.readByte();

                    Message msg = messageFactory.createMessage(messageCode, dataIn);
                    dataIn.close();

                    Receiver receiver;
                    if (this.receivers.containsKey(communicationId)) {
                        synchronized (this) {
                            receiver = this.receivers.remove(communicationId);
                            TimerTask task = tasks.remove(communicationId);
                            if (task != null) {
                                task.cancel();
                            }
                        }
                    } else {
                        receiver = messageFactory.createReceiver(messageCode, this);
                    }

                    if (receiver != null) {
                        receiver.receive(msg, communicationId);
                    }
                }
            } catch (IOException e) {
                System.err.println("Server ran into a problem in listener method. Message: " + e.getMessage());
            }
        }

    }

    private synchronized void unregister(int comm)
    {
        receivers.remove(comm);
        this.tasks.remove(comm);
    }

    class TimeoutTask extends TimerTask
    {

        private final int communicationId;
        private final Receiver receiver;

        public TimeoutTask(int communicationId, Receiver receiver)
        {
            this.communicationId = communicationId;
            this.receiver = receiver;
        }

        @Override
        public void run()
        {
            try
            {
                unregister(communicationId);
                receiver.timeout(communicationId);
            }
            catch (IOException e)
            {
                System.err.println("Cannot unregister a receiver. Message: " + e.getMessage());
            }
        }
    }
}

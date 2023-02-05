package routing;

import core.*;
import routing.util.MessageSet;
import routing.util.RoutingTable;

import java.util.ArrayList;
import java.util.List;

/**
 * OLSR message router
 */
public class OLSRRouter extends ActiveRouter {

    private RoutingTable routingTable;
    private MessageSet messageSet;
    private double lastTcTime;
    private double DEFAULT_TC_INTERVAL = 500.0;
    private boolean isFirstTime = true;
    private double DEFAULT_MESSAGE_STORE_TIME = 200.0;


    /**
     * Constructor. Creates a new message router based on the settings in
     * the given Settings object.
     * @param s The settings object
     */
    public OLSRRouter(Settings s) {
        super(s);
        routingTable = new RoutingTable();
        messageSet = new MessageSet();
        //TODO: read&use OLSR router specific settings (if any)
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected OLSRRouter(OLSRRouter r) {
        super(r);
        routingTable = new RoutingTable();
        messageSet = new MessageSet();
        //TODO: copy OLSR settings here (if any)
    }

    @Override
    public void update() {
        super.update();
//        sendHelloMessages();

        // Check if we need to send a TC message
        double tcInterval = getTcInterval();
//        if (this.getHost().toString().equals("b12")) {
//            System.out.println("\n " + SimClock.getTime() + " " + lastTcTime + " " + tcInterval);
//            System.out.println(" " + (SimClock.getTime() - lastTcTime) + " >=? " + tcInterval);
//            System.out.println(SimClock.getTime() - lastTcTime >= tcInterval);
//        }
        if (SimClock.getTime() - lastTcTime >= tcInterval) {
//            System.out.println("(SimClock.getTime() - lastTcTime >= tcInterval): ");
//            System.out.println(SimClock.getTime() - lastTcTime >= tcInterval);
//            System.out.println(" " + SimClock.getTime() + " " + lastTcTime + " " + tcInterval);
//            System.out.println(" " + (SimClock.getTime() + lastTcTime) + " >= " + tcInterval);
            sendTcMessages();

            lastTcTime = SimClock.getTime();
        }
//        if (isFirstTime) {
//            sendTcMessages();
//            isFirstTime = false;
//        }
//        if (this.getHost().toString().equals("b12")) {
//            List<Connection> connections = this.getConnections();
//            System.out.println("I'm " + this.getHost().toString() + " and I have connections: " + connections.size());
//            System.out.println("I'm " + this.getHost().toString() + " and I have neighbours in routing table: " + routingTable.getNeighborConnections().size());
//        }

        // Check if we have any messages to deliver
        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        // Try to deliver messages to final recipient
        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }

//        System.out.println("stop3");
//        System.out.println("I'm " + this.getHost().toString() + " and I have messages: " + this.getHost().getMessageCollection().isEmpty());
//        // check if we received messages from other nodes
//        if (!this.getHost().getMessageCollection().isEmpty()) {
////            System.out.println("I'm " + this.getHost().toString() + " and I have messages: " + this.getHost().getMessageCollection().toString());
//            return;
//        }

        // Forward messages using OLSR routing table
        forwardMessagesToAllNeighbours();
    }

    private void processTcMessage(Message m) {
        boolean printMode = false;
        if (printMode) {
            System.out.println("processTcMessage:" + m.toString());
        }
        
        String messageID = m.getId();
        int TTL = m.getTtl();
        if (TTL <= 0) {
            return;
        }
        //read routing table from the message
        try {
            String oldRoutingTable = routingTable.toString();
            RoutingTable neighborRoutingTable = (RoutingTable) m.getProperty("routingTable");

            routingTable.merge(this.getHost(), m.getFrom(), neighborRoutingTable);
            String newRoutingTable = routingTable.toString();

            if (!oldRoutingTable.equals(newRoutingTable)) {
                //send the updated routing table to all neighbors
                sendTcMessages();
                if (printMode) {
                    System.out.println("I'm " + this.getHost().toString() + " and my routing table has changed");
                    System.out.println(" old routing table: " + oldRoutingTable);
                    System.out.println(" new routing table: " + newRoutingTable);
                }
            }
        } catch (NullPointerException e) {
            if (printMode) {
                System.out.println("Message " + messageID + " didn't contain a routing table");
            }
        }
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message incoming = removeFromIncomingBuffer(id, from);
        if (incoming == null) {
            throw new SimError("No message with ID " + id + " in the incoming "+
                    "buffer of " + this.getHost());
        }

        boolean isFinalRecipient = incoming.getTo().toString().equals(this.getHost().toString());
//        boolean isFirstDelivery; // is this first delivered instance of this message

        if (isFinalRecipient) {
            //this router is final recipient of this message
            if (incoming.getProperty("type") == null) {
//                System.out.println("Message " + id + " has no 'type' property");
            }
            //if type is tc
            else if (incoming.getProperty("type").equals("TC")) {
//                System.out.println(SimClock.getTime() + " I'm " + this.getHost().toString() + " and I've got TC message ");
                this.processTcMessage(incoming);
            }
        } else {
            //need to forward this message

            System.out.println("I'm " + this.getHost().toString() + " and I'm forwarding message " + incoming.toString());
            String messageID = incoming.getId();
            //beta message set
            if (messageSet.containsMessage(messageID)) {
//            System.out.println(messageID + " is in the messageSet: " + messageSet.toString());
                //print message type
//                RoutingTable neighborRoutingTable = (RoutingTable) incoming.getProperty("routingTable");
//            System.out.println(neighborRoutingTable.toString());
//                return;
            } else {
                //add the message to the message set
//            System.out.println(messageID + " is not in the messageSet: " + messageSet.toString());
                double timeToDie = SimClock.getTime();
                timeToDie += DEFAULT_MESSAGE_STORE_TIME;
                messageSet.addMessage(messageID, timeToDie);
            }
        }

        return incoming;
    }

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);

        if (!con.isUp()) {
            //lost connection
            if (routingTable.contains(con.toNode)) {
                routingTable.removeEntry(con.toNode, SimClock.getTime());
            }
        }
        else if (!this.getHost().toString().equals(con.fromNode.toString())) {
            //received connection
            if (!routingTable.contains(con.toNode)) {
                routingTable.addEntry(con.fromNode, con.fromNode, con, 1, true, SimClock.getTime());
            }
        }
        else {
            //sent connection
            if (!routingTable.contains(con.toNode)) {
                routingTable.addEntry(con.toNode, con.toNode, con, 1, true, SimClock.getTime());
            }
        }
    }

    private void sendHelloMessages() {
        // Implement HELLO message exchange process

        // Do we send hello messages every interval of time?
        return;
    }


    private void sendTcMessages() {
        // Check if we have any active connections with neighbors
        List<Connection> neighborConnections = routingTable.getNeighborConnections();
        if (neighborConnections.isEmpty()) {
            return;
        }
//        System.out.println("I'm " + this.getHost().toString() + " and I HAVE " + neighborConnections.size() + " neighbors");

        if (SimClock.getTime() > 5000.0) {
            return;
        }
//        if (this.getHost().toString().equals("b6")) {
//            System.out.println("I'm " + this.getHost().toString() + " routing table: " + routingTable.toString());
//        }

//        List<Connection> connections = this.getConnections();
//        System.out.println("I'm " + this.getHost().toString() + " and I have connections: " + connections.size());
//        System.out.println("I'm " + this.getHost().toString() + " and I have neighbours in routing table: " + routingTable.getNeighborConnections().size());


        //iterate through all neighbors
        for (Connection neighborConnection : neighborConnections) {
            if (this.getHost().toString().equals(neighborConnection.fromNode.toString())) {
                System.out.println("I'm " + this.getHost().toString() +
                        " sending TC message to " + neighborConnection.toNode.toString());

                //print neighborConnection
                System.out.println("neighborConnection: " + neighborConnection.toString());
                Message m = new Message(
                        this.getHost(),
                        neighborConnection.toNode,
                        "M-" + this.getHost().toString() + '-' +
                                neighborConnection.toNode.toString() + '-' + SimClock.getTime(),
                        1
                );
            } else {
                System.out.println("I'm " + this.getHost().toString() +
                        " sending TC message to " + neighborConnection.fromNode.toString());

                //print neighborConnection
                System.out.println("neighborConnection: " + neighborConnection.toString());
                Message m = new Message(
                        this.getHost(),
                        neighborConnection.fromNode,
                        "M-" + this.getHost().toString() + '-' +
                                neighborConnection.fromNode.toString() + '-' + SimClock.getTime(),
                        1
                );
                m.addProperty("type", "TC");
                m.addProperty("routingTable", routingTable);
                //check if it isn't already in the message collection
                if (this.getHost().isInMessageCollection(m)) {
                    return;
                }
                //add if not
                this.createNewMessage(m);
            }
        }
    }

    private double getTcInterval() {
        // Return the TC interval from the router's settings
        // or a default value if not specified
//        Settings s = getSettings();
//        if (s.contains(TC_INTERVAL_SETTING)) {
//            return s.getDouble(TC_INTERVAL_SETTING);
//        }
        return DEFAULT_TC_INTERVAL;
    }


    private void forwardMessagesToAllNeighbours() {
        // Use OLSR routing table to determine next hop for each message
        // and try to send the message to the next hop

        List<Connection> neighborConnections = routingTable.getNeighborConnections();
        List<Message> messages = new ArrayList<Message>(this.getMessageCollection());
        this.sortByQueueMode(messages);
        System.out.println("I'm " + this.getHost().toString() + " and I have " + neighborConnections.size() +
                " neighbors" + " and I have " + this.getNrofMessages() + " messages");
        if (neighborConnections.size() == 0 || this.getNrofMessages() == 0) {
            return;
        }
        //print all messages
        System.out.println("I'm " + this.getHost().toString() + " and I have messages: " + messages.size());
        for (Message m : messages) {
            System.out.println("I'm " + this.getHost().toString() + " and I have message: " + m.toString());
        }
        this.tryMessagesToConnections(messages, neighborConnections);

        // If the message doesn't have a final recipient or the destination is not a neighbor, forward
        // the message to all neighbors?
    }

    @Override
    public OLSRRouter replicate() {
        return new OLSRRouter(this);
    }

}
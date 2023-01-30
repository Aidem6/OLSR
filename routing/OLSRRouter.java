package routing;

import core.*;
import routing.util.MessageSet;
import routing.util.RoutingTable;
import java.util.List;

/**
 * OLSR message router
 */
public class OLSRRouter extends ActiveRouter {

    private RoutingTable routingTable;
    private MessageSet messageSet;
    private double lastTcTime;
    private double DEFAULT_TC_INTERVAL = 5000.0;
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
//        double tcInterval = getTcInterval();
//        if (SimClock.getTime() - lastTcTime >= tcInterval) {
//            sendTcMessages();
//        }
        if (isFirstTime) {
            sendTcMessages();
            isFirstTime = false;
        }
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

        // check if we received messages from other nodes
        if (!this.getHost().getMessageCollection().isEmpty()) {
//            System.out.println("I'm " + this.getHost().toString() + " and I have messages: " + this.getHost().getMessageCollection().toString());
            return;
        }

        // Forward messages using OLSR routing table
        forwardMessages();
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
        //beta message set
//        if (messageSet.containsMessage(messageID)) {
////            System.out.println(messageID + " is in the messageSet: " + messageSet.toString());
//            //print message type
//            RoutingTable neighborRoutingTable = (RoutingTable) m.getProperty("routingTable");
//            System.out.println(neighborRoutingTable.toString());
//            return;
//        } else {
//            //add the message to the message set
////            System.out.println(messageID + " is not in the messageSet: " + messageSet.toString());
//            double timeToDie = SimClock.getTime();
//            timeToDie += DEFAULT_MESSAGE_STORE_TIME;
//            messageSet.addMessage(messageID, timeToDie);
//        }
        //read routing table from the message
        try {
            String oldRoutingTable = routingTable.toString();
            RoutingTable neighborRoutingTable = (RoutingTable) m.getProperty("routingTable");

            routingTable.merge(this.getHost(), neighborRoutingTable);
            String newRoutingTable = routingTable.toString();

            if (!oldRoutingTable.equals(newRoutingTable) && printMode) {
                System.out.println("I'm " + this.getHost().toString() + " and my routing table has changed");
                System.out.println(" old routing table: " + oldRoutingTable);
                System.out.println(" new routing table: " + newRoutingTable);
            }
        } catch (NullPointerException e) {
            if (printMode) {
                System.out.println("Message " + messageID + " didn't contain a routing table");
            }
            return;
        }
        //send the updated routing table to all neighbors
        sendTcMessages();
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
                this.processTcMessage(incoming);
            }
        } else {
            //need to forward this message
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

        // Does function changedConnection assure us that every new connection is found in here and we only
        // have to establish routing table once at the beginning?

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


//        if (!this.getHost().toString().equals("b12")) {
//            return;
//        }

//        List<Connection> connections = this.getConnections();
//        System.out.println("I'm " + this.getHost().toString() + " and I have connections: " + connections.size());
//        System.out.println("I'm " + this.getHost().toString() + " and I have neighbours in routing table: " + routingTable.getNeighborConnections().size());


        // Create a new TC message
        Message m = new Message(
                this.getHost(),
                routingTable.getNeighborConnections().get(0).toNode,
                "M-" + this.getHost().toString() + '-' +
                        routingTable.getNeighborConnections().get(0).toNode.toString() + '-' + SimClock.getTime(),
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
//        System.out.println("I'm " + this.getHost().toString() + " and I've created a TC message: " + m.toString());
//        System.out.println(this.getHost().getMessageCollection().toString());
        // Send the TC message to all active connections
//        System.out.println("I'm " + this.getHost().toString() + " and I have " + neighborConnections.size() + " neighbors");
        for (Connection con : neighborConnections) {
//            System.out.println("I'm " + this.getHost().toString() + " and I'm sending a TC message to " + con.toNode);
//                tryToSendMessage(tcMessage.getId(), con, true);
//                System.out.println("I'm " + this.getHost().toString() + " and I've sent a TC message to " + con.toNode);
//                System.out.println(this.getHost().getMessageCollection().toString());
        }

        // Update the last TC time
        lastTcTime = SimClock.getTime();
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


    private void forwardMessages() {
        // Use OLSR routing table to determine next hop for each message
        // and try to send the message to the next hop

        this.tryAllMessagesToAllConnections();

        // If the message doesn't have a final recipient or the destination is not a neighbor, forward
        // the message to all neighbors?
        return;
    }

    @Override
    public OLSRRouter replicate() {
        return new OLSRRouter(this);
    }

}
package routing;

import core.Settings;
import core.Connection;
import routing.util.RoutingTable;
import java.util.List;
import java.util.Map;
import java.util.Map;
import java.util.HashMap;
import core.Message;
import core.SimClock;
import core.DTNHost;
import core.MessageListener;

/**
 * OLSR message router
 */
public class OLSRRouter extends ActiveRouter {

    private RoutingTable routingTable;
    private double lastTcTime;
    private double DEFAULT_TC_INTERVAL = 50.0;


    /**
     * Constructor. Creates a new message router based on the settings in
     * the given Settings object.
     * @param s The settings object
     */
    public OLSRRouter(Settings s) {
        super(s);
        routingTable = new RoutingTable();
        //TODO: read&use OLSR router specific settings (if any)
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected OLSRRouter(OLSRRouter r) {
        super(r);
        routingTable = new RoutingTable();
        //TODO: copy OLSR settings here (if any)
    }

    @Override
    public void update() {
//        super.update();

        // Check if we need to send HELLO or TC messages
//        sendHelloMessages();
//        sendTcMessages();
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

        // Forward messages using OLSR routing table
//        forwardMessages();
//        this.tryAllMessagesToAllConnections();
    }

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);

//        if (!this.getHost().toString().equals("b12")) {
//            return;
//        }
        if (!this.getHost().toString().equals(con.fromNode.toString())) {
            System.out.println("I'm " + this.getHost().toString() + " and I've received a connection from " + con.fromNode);
            return;
        }
//        System.out.println("Time: " + SimClock.getTime());
        if (con.isUp()) {
//            System.out.println("I'm " + this.getHost().toString() + " and I've sent a new connection to " + con.toNode);

            if (routingTable.contains(con.toNode)) {
                return;
            }
            routingTable.addEntry(con.toNode, con.toNode, con, 1, true, SimClock.getTime());
        }
        else {
//            System.out.println("I'm " + this.getHost().toString() + " and I've lost a connection to " + con.toNode);

            if (!routingTable.contains(con.toNode)) {
                return;
            }
            routingTable.removeEntry(con.toNode, SimClock.getTime());
        }
//        System.out.println(routingTable.toString());

//        if (routingTable.contains(con.fromNode) || routingTable.contains(con.toNode)) {
//            return;
//        }

//        if (this.getHost().toString().equals(con.fromNode.toString())) {
////            System.out.println("I'm " + this.getHost().toString() + " and I've sent a new connection to " + con.toNode);routingTable.addEntry(con.fromNode, con.fromNode, con, 1, true);
//
//            routingTable.addEntry(con.toNode, con.toNode, con, 1, true);
////            if (this.getHost().toString().equals("b12")) {
////                System.out.println("I'm " + this.getHost().toString() + " and I have new connection with " + con.toNode);
////                System.out.println(routingTable.toString());
////                System.out.println(routingTable.getDestinations().toString());
////                System.out.println(routingTable.getNeighbors().toString());
////            }
//        }
//        else {
//            if (this.getHost().toString().equals("b12"))
//                System.out.println("I'm " + this.getHost().toString() + " and I've received a new connection from " + con.fromNode);
//        }
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

        // Check if we need to send a TC message
        double tcInterval = getTcInterval();
        if (SimClock.getTime() - lastTcTime < tcInterval) {
            return;
        }

        if (this.getHost().toString().equals("b12")) {
            //get connections
            List<Connection> connections = this.getConnections();
            System.out.println("I'm " + this.getHost().toString() + " and I have connections: " + connections.size());
            System.out.println("I'm " + this.getHost().toString() + " and I have neighbours in routing table: " + routingTable.getNeighborConnections().size());


//            // Create a new TC message
//            Message m = new Message(this.getHost(), routingTable.getNeighborConnections().get(0).toNode, this.getHost().toString(), 1);
//            //check if it isn't already in the message collection
//            if (this.getHost().isInMessageCollection(m)) {
//                return;
//            }
//            //add if not
//            this.createNewMessage(m);
//            System.out.println("I'm " + this.getHost().toString() + " and I've created a TC message: " + m.toString());
////            System.out.println(this.getHost().getMessageCollection().toString());
//            // Send the TC message to all active connections
//            System.out.println("I'm " + this.getHost().toString() + " and I have " + neighborConnections.size() + " neighbors");
//            for (Connection con : neighborConnections) {
//                System.out.println("I'm " + this.getHost().toString() + " and I'm sending a TC message to " + con.toNode);
////                tryToSendMessage(tcMessage.getId(), con, true);
////                System.out.println("I'm " + this.getHost().toString() + " and I've sent a TC message to " + con.toNode);
////                System.out.println(this.getHost().getMessageCollection().toString());
//            }
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

//        this.tryAllMessagesToAllConnections();

        // If the message doesn't have a final recipient or the destination is not a neighbor, forward
        // the message to all neighbors?
        return;
    }

    @Override
    public OLSRRouter replicate() {
        return new OLSRRouter(this);
    }

}
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
    private double DEFAULT_TC_INTERVAL = 5.0;


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
        super.update();

        // Check if we need to send HELLO or TC messages
//        sendHelloMessages();
//        sendTcMessages();

        // Check if we have any messages to deliver
        if (isTransferring() || !canStartTransfer()) {
            return; // transferring, don't try other connections yet
        }

        // Try to deliver messages to final recipient
        if (exchangeDeliverableMessages() != null) {
            return; // started a transfer, don't try others (yet)
        }

        // Forward messages using OLSR routing table
        forwardMessages();
    }

    @Override
    public void changedConnection(Connection con) {
        super.changedConnection(con);
        if (this.getHost().toString().equals(con.fromNode.toString())) {
//            System.out.println("I'm " + this.getHost().toString() + " and I've sent a new connection to " + con.toNode);
        }
        else {
            System.out.println("I'm " + this.getHost().toString() + " and I've received a new connection from " + con.fromNode);

            routingTable.addEntry(con.fromNode, con.fromNode, con, 1, true);
            System.out.println(routingTable.toString());
            System.out.println(routingTable.getDestinations().toString());
            System.out.println(routingTable.getNeighbors().toString());
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
        // Check if we have any active connections
        List<Connection> connections = getConnections();
        System.out.println("I'm " + this.getHost().toString() + " and I have " + connections.size() + " connections");
        if (connections.isEmpty()) {
            return;
        }

        // Check if we need to send a TC message
        double tcInterval = getTcInterval();
        if (SimClock.getTime() - lastTcTime < tcInterval) {
            return;
        }

        // Create a new TC message
        Message tcMessage = createTcMessage();
        addToMessages(tcMessage, false);

        // Send the TC message to all active connections
        for (Connection con : connections) {
            if (con.isUp()) {
//                tryToSendMessage(tcMessage.getId(), con, true);
//                System.out.println("I'm " + this.getHost().toString() + " and I've sent a TC message to " + con.toNode);
//                System.out.println(this.getHost().getMessageCollection().toString());

            }
        }

        // Update the last TC time
        lastTcTime = SimClock.getTime();
    }

    private Message createTcMessage() {
        // Create a new TC message and add entries for each destination
        // in the router's routing table
        Message tcMessage = new Message(getHost(), null, "TC", 0);
        Map<DTNHost, Integer> entries = new HashMap<>();
        for (DTNHost destination : routingTable.getDestinations()) {
            DTNHost nextHop = routingTable.getNextHop(destination);
            int hopCount = routingTable.getHopCount(destination);
            entries.put(destination, hopCount);
        }
        tcMessage.addProperty("entries", entries);
        return tcMessage;
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
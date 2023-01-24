package routing;

import core.Settings;
import core.Connection;
//import routing.util.RoutingTable;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.lang.StringBuilder;
import core.DTNHost;

/**
 * OLSR message router
 */
public class OLSRRouter extends ActiveRouter {

    private static class RoutingEntry {
        String neighbor;
        String nextHop;
        int hopCount;

        public RoutingEntry(String neighbor, String nextHop, int hopCount) {
            this.neighbor = neighbor;
            this.nextHop = nextHop;
            this.hopCount = hopCount;
        }
    }
    private ArrayList<RoutingEntry> routingTable = new ArrayList<RoutingEntry>();

//    private RoutingTable routingTable;

    /**
     * Constructor. Creates a new message router based on the settings in
     * the given Settings object.
     * @param s The settings object
     */
    public OLSRRouter(Settings s) {
        super(s);
        //TODO: read&use OLSR router specific settings (if any)
    }

    /**
     * Copy constructor.
     * @param r The router prototype where setting values are copied from
     */
    protected OLSRRouter(OLSRRouter r) {
        super(r);
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
        if (this.getHost().toString().equals(con.fromNode.toString())) {
            System.out.println("I'm " + this.getHost().toString() + " and I've sent a new connection to " + con.toNode);
        }
        else {
            System.out.println("I'm " + this.getHost().toString() + " and I've received a new connection from " + con.fromNode);

            RoutingEntry entry = new RoutingEntry(con.fromNode.toString(), con.fromNode.toString(), 1);
            routingTable.add(entry);

//            routingTable.addEntry(con.fromNode, con.fromNode, 1);
//            System.out.println(con.fromNode.getClass().getName());
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
        // Implement TC message exchange process

        // Check if we have any active connections
        // Check if we need to send a TC message
        // Send the TC message to all active connections
        return;
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
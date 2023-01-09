package routing;

import core.Settings;

/**
 * OLSR message router
 */
public class OLSRRouter extends ActiveRouter {

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

//    @Override
//    public void changedConnection(Connection con) {
//        if (this.energy != null && con.isUp() && !con.isInitiator(getHost())) {
//            this.energy.reduceDiscoveryEnergy();
//        }
//    }

    @Override
    public void update() {
        super.update();

        // Check if we need to send HELLO or TC messages
        sendHelloMessages();
        sendTcMessages();

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

    private void sendHelloMessages() {
        // Implement HELLO message exchange process

        // Does function changedConnection assure us that every new connection is found in here and we only
        // have to establish routing table once at the beginning?

        // Do we send hello messages every interval of time?
    }

    private void sendTcMessages() {
        // Implement TC message exchange process

        // Check if we have any active connections
        // Check if we need to send a TC message
        // Send the TC message to all active connections
    }

    private void forwardMessages() {
        // Use OLSR routing table to determine next hop for each message
        // and try to send the message to the next hop

        // If the message doesn't have a final recipient or the destination is not a neighbor, forward
        // the message to all neighbors?
    }

    @Override
    public OLSRRouter replicate() {
        return new OLSRRouter(this);
    }

}
package routing.util;

import core.DTNHost;
import core.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.lang.StringBuilder;


public class RoutingTable {

    private Map<DTNHost, RoutingEntry> table;
    public double maxTimeToLive = 0;
    public List<Double> timeToLiveList = new ArrayList<>();

    public RoutingTable() {
        this.table = new HashMap<>();
    }

    public void addEntry(DTNHost destination, DTNHost nextHop, Connection nextHopConnection, int hopCount, boolean isNeighbor, double time) {
        RoutingEntry entry = new RoutingEntry(destination, nextHop, nextHopConnection, hopCount, isNeighbor, time);
        table.put(destination, entry);
    }

    public void removeEntry(DTNHost destination, double endTime) {
        RoutingEntry entry = table.get(destination);
        if (entry == null) {
            return;
        }
        timeToLiveList.add(endTime - entry.time);
        if (endTime - entry.time > maxTimeToLive) {
            maxTimeToLive = endTime - entry.time;
            System.out.println("Max time to live: " + maxTimeToLive);
        }
        double sum = 0;
        for (double timeToLive : timeToLiveList) {
            sum += timeToLive;
        }
        System.out.println("Mean time to live: " + sum / timeToLiveList.size());
        table.remove(destination);
    }

    public DTNHost getNextHop(DTNHost destination) {
        RoutingEntry entry = table.get(destination);
        if (entry == null) {
            return null;
        }
        return entry.nextHop;
    }

    public Connection getNextHopConnection(DTNHost destination) {
        RoutingEntry entry = table.get(destination);
        if (entry == null) {
            return null;
        }
        return entry.nextHopConnection;
    }

    public int getHopCount(DTNHost destination) {
        RoutingEntry entry = table.get(destination);
        if (entry == null) {
            return -1;
        }
        return entry.hopCount;
    }

    public Set<DTNHost> getDestinations() {
        return table.keySet();
    }

    public Set<DTNHost> getNeighbors() {
        Set<DTNHost> destinations = getDestinations();
        Set<DTNHost> neighbors = new HashSet<>();
        for (DTNHost destination : destinations) {
            RoutingEntry entry = table.get(destination);
            if (entry.isNeighbor) {
                neighbors.add(destination);
            }
        }
        return neighbors;
    }

    public List<Connection> getNeighborConnections() {
        Set<DTNHost> destinations = getDestinations();
        List<Connection> neighborConnections = new ArrayList<>();
        for (DTNHost destination : destinations) {
            RoutingEntry entry = table.get(destination);
            if (entry.isNeighbor) {
                neighborConnections.add(entry.nextHopConnection);
            }
        }
        return neighborConnections;
    }

    public boolean contains(DTNHost destination) {
        return table.containsKey(destination);
    }


    private static class RoutingEntry {
        DTNHost destination;
        DTNHost nextHop;
        Connection nextHopConnection;
        boolean isNeighbor;
        int hopCount;
        double time;

        public RoutingEntry(DTNHost destination, DTNHost nextHop, Connection nextHopConnection, int hopCount, boolean isNeighbor, double time) {
            this.destination = destination;
            this.nextHop = nextHop;
            this.nextHopConnection = nextHopConnection;
            this.hopCount = hopCount;
            this.isNeighbor = isNeighbor;
            this.time = time;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Routing Table:\n");
        for (DTNHost destination : table.keySet()) {
            RoutingEntry entry = table.get(destination);
            sb.append(String.format(" destination: %s | next hop: %s | hop count: %d | isNeighbor : %b \n",
                    destination, entry.nextHop, entry.hopCount, entry.isNeighbor));
        }
        return sb.toString();
    }
}

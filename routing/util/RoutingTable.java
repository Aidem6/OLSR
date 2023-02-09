package routing.util;

import core.DTNHost;
import core.Connection;
import core.SimClock;

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
//            System.out.println("Max time to live: " + maxTimeToLive);
        }
        double sum = 0;
        for (double timeToLive : timeToLiveList) {
            sum += timeToLive;
        }
//        System.out.println("Mean time to live: " + sum / timeToLiveList.size());
        table.remove(destination);
    }

    // merge two routing tables into one
    public void merge(DTNHost selfHost, DTNHost fromHost, RoutingTable otherTable) {
        boolean printMode = false;
        Set<DTNHost> otherDestinations = otherTable.getDestinations();
        if (printMode) {
            System.out.println("\n\ntable before: " + table.toString());
            System.out.println("selfHost: " + selfHost.toString());
            System.out.println("otherTable: " + otherTable.toString());
        }
        for (DTNHost destination : otherDestinations) {
            RoutingEntry otherEntry = otherTable.table.get(destination);
            RoutingEntry entry = table.get(destination);
            if (otherEntry.nextHop == selfHost) {
            }
            else if (entry == null) {
                otherEntry.isNeighbor = false;
                otherEntry.hopCount++;
                otherEntry.nextHop = fromHost;
                table.put(destination, otherEntry);
                if (printMode) {
                    System.out.println("entry is null");
                    System.out.println("table after: " + table.toString());
                }
//                System.out.println(selfHost.toString() + " new con to " + otherEntry.destination + " " + otherEntry.hopCount);
            }
            else if (entry.hopCount > (otherEntry.hopCount + 1)) {
                otherEntry.isNeighbor = false;
                otherEntry.hopCount++;
                otherEntry.nextHop = fromHost;
                table.put(destination, otherEntry);
                if (printMode) {
                    System.out.println("hop count shortcut");
                    System.out.println("otherEntry: " + otherEntry.toString());
                    System.out.println("table after: " + table.toString());
                }
//                System.out.println(selfHost.toString() + " UPDATED con from " + entry.destination + " " + entry.hopCount);
//                System.out.println(selfHost.toString() + " UPDATED con to " + otherEntry.destination + " " + otherEntry.hopCount);
            }
        }
    }

    //function that deletes inactive connections
    public void clean() {
        String tableBefore = table.toString();
        Set<DTNHost> toRemove = new HashSet<>();
        for (RoutingEntry entry : table.values()) {
            if (!entry.nextHopConnection.isUp()) {
                toRemove.add(entry.destination);
            }
        }
        for (DTNHost destination : toRemove) {
            this.removeEntry(destination, SimClock.getTime());
        }
//        if (!tableBefore.equals(table.toString())) {
//            System.out.println("\nclean\nbefore:\n" + tableBefore);
//            System.out.println("after:\n" + table.toString());
//            System.out.println("\n");
//        }
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

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("\n destination: %s | next hop: %s | hop count: %d | isNeighbor : %b",
                    destination, nextHop, hopCount, isNeighbor));
            return sb.toString();
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

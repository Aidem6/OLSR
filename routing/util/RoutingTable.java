package routing.util;

import core.DTNHost;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.lang.StringBuilder;


public class RoutingTable {

    private Map<DTNHost, RoutingEntry> table;

    public RoutingTable() {
        this.table = new HashMap<>();
    }

    public void addEntry(DTNHost neighbor, DTNHost nextHop, int hopCount) {
        RoutingEntry entry = new RoutingEntry(neighbor, nextHop, hopCount);
        table.put(neighbor, entry);
    }

    public void removeEntry(DTNHost neighbor) {
        table.remove(neighbor);
    }

    public DTNHost getNextHop(DTNHost destination) {
        RoutingEntry entry = table.get(destination);
        if (entry == null) {
            return null;
        }
        return entry.nextHop;
    }

    public int getHopCount(DTNHost destination) {
        RoutingEntry entry = table.get(destination);
        if (entry == null) {
            return -1;
        }
        return entry.hopCount;
    }

    public Set<DTNHost> getNeighbors() {
        return table.keySet();
    }

    private static class RoutingEntry {
        DTNHost neighbor;
        DTNHost nextHop;
        int hopCount;

        public RoutingEntry(DTNHost neighbor, DTNHost nextHop, int hopCount) {
            this.neighbor = neighbor;
            this.nextHop = nextHop;
            this.hopCount = hopCount;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Routing Table:\n");
        for (DTNHost neighbor : table.keySet()) {
            RoutingEntry entry = table.get(neighbor);
            sb.append(String.format(" dest: %s | next: %s | hop count: %d\n",
                    neighbor, entry.nextHop, entry.hopCount));
        }
        return sb.toString();
    }
}

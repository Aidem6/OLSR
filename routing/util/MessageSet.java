package routing.util;

import java.util.HashMap;
import java.util.Map;

public class MessageSet {
    private Map<String, MessageInfo> messageSet;

    public MessageSet() {
        this.messageSet = new HashMap<>();
    }

    public void addMessage(String messageID, double timeToDie) {
        MessageInfo messageInfo = new MessageInfo(timeToDie, messageID);
        messageSet.put(messageID, messageInfo);
    }

    public void removeMessage(String messageID) {
        messageSet.remove(messageID);
    }

    public boolean containsMessage(String messageID) {
        try {
            return messageSet.containsKey(messageID);
        }
        catch(NullPointerException e) {
            return false;
        }
    }

    public double getTimeToDie(String messageID) {
        return messageSet.get(messageID).timeToDie;
    }

    public String getMessageID(String messageID) {
        return messageSet.get(messageID).messageID;
    }

    public int size() {
        return messageSet.size();
    }

    public void clear() {
        messageSet.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String messageID : messageSet.keySet()) {
            sb.append(messageID + " ");
        }
        return sb.toString();
    }
}
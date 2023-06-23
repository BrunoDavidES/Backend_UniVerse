package util;

import java.util.ArrayList;
import java.util.List;

public class MessageData {
    private String senderId;
    private List<String> recipientIds = new ArrayList<>();
    private String message;

    public MessageData() {

    }

    public MessageData(String senderId, List<String> recipientIds, String message) {
        this.senderId = senderId;
        this.recipientIds = recipientIds;
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public List<String> getRecipientIds() {
        return recipientIds;
    }

    public void setRecipientIds(List<String> recipientIds) {
        this.recipientIds = recipientIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

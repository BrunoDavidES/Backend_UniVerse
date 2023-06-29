package models;

import java.util.ArrayList;
import java.util.List;

public class MessageData {
    private String chatId;
    private String senderId;
    private List<String> recipientIds = new ArrayList<>();
    private String message;

    public MessageData() {

    }

    public boolean validate() {
        return senderId != null && message != null && (recipientIds.size() == 1 || (recipientIds.size() > 1 && chatId != null));
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

    public String getChatId() { return chatId; }

    public void setChatId(String chatId) { this.chatId = chatId; }
}

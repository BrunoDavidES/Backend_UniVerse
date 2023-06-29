package models;

import java.util.ArrayList;
import java.util.List;

public class MessageData {
    private String chatId;
    private String senderId;
    private String recipientId;
    private String message;

    public MessageData() {}

    public boolean validate() {
        return senderId != null && recipientId != null && message != null;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientIds(String recipientId) {
        this.recipientId = recipientId;
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

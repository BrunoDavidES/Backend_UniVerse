package models;

import com.google.cloud.Timestamp;

public class MessageData {
    private String senderId;
    private String recipientId;
    private String message;
    private Timestamp sentTime;

    public MessageData() {}

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getSentTime() {
        return sentTime;
    }

    public void setSentTime(Timestamp sentTime) {
        this.sentTime = sentTime;
    }

    public boolean validate() {
        return recipientId != null && message != null;
    }

    public void setSent(String senderId) {
        this.senderId = senderId;
        this.recipientId = null;
        this.sentTime = Timestamp.now();
    }
}

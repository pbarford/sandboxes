package resilience.hazelcast;

import java.io.Serializable;

enum MessageType { INIT, FAILOVER, FAILBACK, QUEUE_SWITCH }
public class ClusterMessage implements Serializable {
    private String queueId;
    private String recipientUuid;
    private MessageType messageType;
    private String message;

    public String getRecipientUuid() {
        return recipientUuid;
    }

    public void setRecipientUuid(String recipientUuid) {
        this.recipientUuid = recipientUuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "ClusterMessage{" +
                "queueId='" + queueId + '\'' +
                ", recipientUuid='" + recipientUuid + '\'' +
                ", messageType=" + messageType +
                ", message='" + message + '\'' +
                '}';
    }
}

package resilience.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import resilience.rabbit.ContainerManager;

public class ClusterMessageListener implements MessageListener<ClusterMessage> {

    private final String thisUuid;
    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, String> memberUuidToNodeMapping;
    private final IMap<String, String> queueToNodeMapping;
    private final ContainerManager containerManager;

    public ClusterMessageListener(HazelcastInstance hazelcastInstance,
                                  IMap<String, String> memberUuidToNodeMapping,
                                  IMap<String, String> queueToNodeMapping,
                                  ContainerManager containerManager) {
        this.hazelcastInstance = hazelcastInstance;
        this.thisUuid = hazelcastInstance.getCluster().getLocalMember().getUuid().toString();
        this.memberUuidToNodeMapping = memberUuidToNodeMapping;
        this.queueToNodeMapping = queueToNodeMapping;
        this.containerManager = containerManager;
    }

    @Override
    public void onMessage(Message<ClusterMessage> message) {

        if(message.getMessageObject().getRecipientUuid() != null && message.getMessageObject().getRecipientUuid().equalsIgnoreCase(thisUuid)) {
            processMessageForThisNode(message);
        }
        String fromNodeId = memberUuidToNodeMapping.get(message.getPublishingMember().getUuid());
        if(!message.getPublishingMember().getUuid().toString().equalsIgnoreCase(thisUuid))
            System.out.println( "from " + fromNodeId + " --> " + message.getMessageObject());
        for(String uuid : memberUuidToNodeMapping.keySet()) {
            System.out.println(memberUuidToNodeMapping.get(uuid) + " --> " + uuid);
        }
        for(String queue : queueToNodeMapping.keySet()) {
            System.out.println(queueToNodeMapping.get(queue) + " --> " + queue);
        }
    }

    private void processMessageForThisNode(Message<ClusterMessage> message) {
        if(message.getMessageObject().getMessageType() == MessageType.FAILOVER) {
            processFailOver(message);
        }
        if(message.getMessageObject().getMessageType() == MessageType.FAILBACK) {
            processFailBack(message);
        }
    }

    private void processFailOver(Message<ClusterMessage> message) {
        switchQueue(message.getMessageObject().getQueueId());
        ClusterMessage msg = new ClusterMessage();
        msg.setMessageType(MessageType.QUEUE_SWITCH);
        hazelcastInstance.getTopic("clusterTopic").publish(msg);
    }

    private void processFailBack(Message<ClusterMessage> message) {
        System.out.println("failback Q [" + message.getMessageObject().getQueueId()+"] to [" + message.getPublishingMember().getUuid().toString() + "]");
        ClusterMessage msg = new ClusterMessage();
        containerManager.removeContainerFor(message.getMessageObject().getQueueId());
        msg.setRecipientUuid(message.getPublishingMember().getUuid().toString());
        msg.setQueueId(message.getMessageObject().getQueueId());
        msg.setMessageType(MessageType.FAILOVER);
        hazelcastInstance.getTopic("clusterTopic").publish(msg);
    }


    private void switchQueue(String q) {
        System.out.println("switching Q [" + q + "] to this member");
        if(containerManager.containers().get(q) == null)
            containerManager.createContainerFor(q);
        containerManager.containers().get(q).start();
        queueToNodeMapping.set(q, memberUuidToNodeMapping.get(thisUuid));
    }
}

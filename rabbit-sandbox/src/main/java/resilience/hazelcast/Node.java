package resilience.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import resilience.rabbit.ContainerManager;
import resilience.rabbit.RabbitConfiguration;

@Slf4j
public class Node {

    private final String nodeId;
    private final String[] queues;
    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, String> memberUuidToNodeMapping;
    private final IMap<String, String> queueToNodeMapping;
    private final ContainerManager containerManager;
    private final ApplicationContext context;

    public static void main(String[] args) {
        String nodeId = System.getProperty("nodeId");
        String[] queues = System.getProperty("queueNames").split(",");
        new Node(nodeId, queues);
    }

    public Node(String nodeId, String[] queues) {
        this.nodeId = nodeId;
        this.queues = queues;
        System.out.println("starting : " + nodeId);
        context = new AnnotationConfigApplicationContext(RabbitConfiguration.class);
        hazelcastInstance = context.getBean("hazelcastInstance", HazelcastInstance.class);

        memberUuidToNodeMapping = context.getBean("memberUuidToNodeMapping", IMap.class);
        queueToNodeMapping = context.getBean("queueToNodeMapping", IMap.class);
        containerManager = context.getBean("containerManager", ContainerManager.class);
        initListeners();
        memberUuidToNodeMapping.put(hazelcastInstance.getCluster().getLocalMember().getUuid(), nodeId);
        if(canConsume())
            consumeQueues();
        else
            requestFailBack();

        ClusterMessage msg = new ClusterMessage();
        msg.setMessageType(MessageType.INIT);
        hazelcastInstance.getTopic("clusterTopic").publish(msg);
        while(true) {
        }
    }

    private void initListeners() {

        hazelcastInstance.getCluster().addMembershipListener(new ClusterMemberListener(hazelcastInstance,
                                                                                       memberUuidToNodeMapping,
                                                                                       queueToNodeMapping,
                                                                                       containerManager));

        ClusterMessageListener list = new ClusterMessageListener(hazelcastInstance,
                                                                 memberUuidToNodeMapping,
                                                                 queueToNodeMapping,
                                                                 containerManager);
        ITopic<ClusterMessage> topic = hazelcastInstance.getTopic("clusterTopic");
        topic.addMessageListener(list);
    }

    private boolean canConsume() {
        return !queueToNodeMapping.containsKey(queues[0]);
    }

    private void requestFailBack() {

        for(String q : queues) {
            String recipientUuid = getUuidForNodeId(queueToNodeMapping.get(q));
            ClusterMessage message = new ClusterMessage();
            message.setRecipientUuid(recipientUuid);
            message.setMessageType(MessageType.FAILBACK);
            message.setQueueId(q);
            hazelcastInstance.getTopic("clusterTopic").publish(message);
        }
    }

    private String getUuidForNodeId(String nodeId) {
        for(String uuid : memberUuidToNodeMapping.keySet()) {
            if(memberUuidToNodeMapping.get(uuid).equalsIgnoreCase(nodeId))
                return uuid;
        }
        return null;
    }

    private void consumeQueues() {
        for(String q: queues) {
            containerManager.containers().get(q).start();
            queueToNodeMapping.put(q, nodeId);
        }
    }
}

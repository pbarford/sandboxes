package hazelcast;

import com.hazelcast.core.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class ClusterMemberListener implements MembershipListener {

    private final HazelcastInstance hazelcastInstance;
    private final IMap<String, String> memberUuidToNodeMapping;
    private final IMap<String, String> queueToNodeMapping;

    public ClusterMemberListener(HazelcastInstance hazelcastInstance,
                                 IMap<String, String> memberUuidToNodeMapping,
                                 IMap<String, String> queueToNodeMapping) {
        this.hazelcastInstance = hazelcastInstance;
        this.memberUuidToNodeMapping = memberUuidToNodeMapping;
        this.queueToNodeMapping = queueToNodeMapping;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {

        System.out.println("memberAdded --> " + membershipEvent.getMember().getUuid());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {

        Lock lock = hazelcastInstance.getLock(membershipEvent.getMember().getUuid().toString());
        try {
            if (lock.tryLock (3, TimeUnit.SECONDS)) {
                try {
                    String nodeId = memberUuidToNodeMapping.get(membershipEvent.getMember().getUuid());
                    if (nodeId != null) {
                        System.out.println("memberRemoved --> " + membershipEvent.getMember().getUuid() + " = " + nodeId);
                        failOverQueues(getQueuesToFailOver(nodeId));
                        memberUuidToNodeMapping.remove(membershipEvent.getMember().getUuid());
                        ClusterMessage msg = new ClusterMessage();
                        msg.setMessageType(MessageType.QUEUE_SWITCH);
                        hazelcastInstance.getTopic("clusterTopic").publish(msg);
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void failOverQueues(List<String> queuesToFailOver) {
        int split = queuesToFailOver.size() / hazelcastInstance.getCluster().getMembers().size();
        for(int i =0; i < queuesToFailOver.size(); i++) {
            if(i < split)
                switchQueue(queuesToFailOver.get(i));
            else
                switchQueueToOtherNode(queuesToFailOver.get(i));
        }
    }

    private List<String> getQueuesToFailOver(String nodeId) {
        List<String> queuesToFailOver = new ArrayList<String>();
        for (String q : queueToNodeMapping.keySet()) {
            if (queueToNodeMapping.get(q).equalsIgnoreCase(nodeId)) {
                queuesToFailOver.add(q);
            }
        }
        return queuesToFailOver;
    }

    private void switchQueue(String q) {
        System.out.println("switching Q [" + q + "] to this member");
        String thisUuid = hazelcastInstance.getCluster().getLocalMember().getUuid().toString();
        queueToNodeMapping.set(q, memberUuidToNodeMapping.get(thisUuid));
    }

    private void switchQueueToOtherNode(String q) {

        String otherNodeUuid = getOtherNodeUuid();
        if(otherNodeUuid!=null) {
            System.out.println("switching Q [" + q + "] to other member [" + otherNodeUuid + "]");
            ClusterMessage msg = new ClusterMessage();
            msg.setRecipientUuid(otherNodeUuid);
            msg.setMessageType(MessageType.FAILOVER);
            msg.setQueueId(q);
            hazelcastInstance.getTopic("clusterTopic").publish(msg);
        }
    }

    private String getOtherNodeUuid() {
        for(Member member : hazelcastInstance.getCluster().getMembers()) {
            if(!member.getUuid().toString().equalsIgnoreCase(hazelcastInstance.getCluster().getLocalMember().getUuid().toString())) {
                return member.getUuid().toString();
            }
        }
        return null;
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        System.out.println("memberAttributeChanged --> " + memberAttributeEvent.getMember().getUuid());
    }
}

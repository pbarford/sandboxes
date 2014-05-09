package hazelcast;

import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class ClusterMemberListener implements MembershipListener {
    @Override
    public void memberAdded(MembershipEvent membershipEvent) {

        System.out.println("memberAdded --> " + membershipEvent.getMember().getUuid());
    }

    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {

        System.out.println("memberRemoved --> " + membershipEvent.getMember().getUuid());
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        System.out.println("memberAttributeChanged --> " + memberAttributeEvent.getMember().getUuid());
    }
}

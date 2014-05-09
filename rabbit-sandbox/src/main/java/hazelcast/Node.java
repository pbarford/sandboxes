package hazelcast;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Node {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:hazelCast.xml");
        context.getBean("hazelcastInstance");
//        Config config = new Config();
//        config.addListenerConfig(new ListenerConfig().setImplementation(new ClusterMemberListener()));
//        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        while(true) {
        }

    }
}

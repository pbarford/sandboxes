package resilience.rabbit;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ContainerManager {

    private Map<String, SimpleMessageListenerContainer> simpleMessageListenerContainers = new HashMap<String, SimpleMessageListenerContainer>();

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private AmqpAdmin amqpAdmin;

    private TaskExecutor createTaskExecutor(String queueName) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.setQueueCapacity(1);
        threadPoolTaskExecutor.setThreadNamePrefix(queueName+"-thread-");
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    private ChannelAwareMessageListener createMessageListener() {
        return new RabbitMqListener();
    }

    public synchronized SimpleMessageListenerContainer createContainerFor(String queueName) {
        if(!simpleMessageListenerContainers.containsKey(queueName)) {
            SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
            simpleMessageListenerContainer.setPrefetchCount(64);
            simpleMessageListenerContainer.setExclusive(true);
            simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            simpleMessageListenerContainer.setTaskExecutor(createTaskExecutor(queueName));
            simpleMessageListenerContainer.setMessageListener(createMessageListener());
            simpleMessageListenerContainer.addQueues(createQueue(queueName));
            simpleMessageListenerContainers.put(queueName, simpleMessageListenerContainer);
        }
        return simpleMessageListenerContainers.get(queueName);
    }

    private Queue createQueue(String name) {
        Queue q = new Queue(name, false, false, false);
        amqpAdmin.declareQueue(q);
        return q;
    }

    public Map<String, SimpleMessageListenerContainer> containers() {
        return Collections.unmodifiableMap(simpleMessageListenerContainers);
    }

    public synchronized void removeContainerFor(String queueName) {
        if(simpleMessageListenerContainers.containsKey(queueName)) {
            System.out.println("stopping container for " + queueName);
            simpleMessageListenerContainers.get(queueName).stop();
            simpleMessageListenerContainers.remove(queueName);
        }
    }
}

package rabbit;

import com.rabbitmq.client.Channel;
import model.Event;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Reactor;
import reactor.core.processor.Processor;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfiguration {

    @Autowired
    private ConnectionFactory cachingConnectionFactory;

    @Autowired
    private Reactor eventLevelReactor;

    @Autowired
    private Reactor messageAcknowledgeReactor;

    @Autowired
    private Processor<Event> eventProcessorLMAX;

    @Autowired
    protected AmqpAdmin amqpAdmin;

    @PostConstruct
    public void init() {
        cachingConnectionFactory.createConnection();
    }

    @Bean
    public SimpleMessageListenerContainer consumer1() {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(cachingConnectionFactory);
        simpleMessageListenerContainer.setPrefetchCount(-1);
        simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener(eventLevelReactor, messageAcknowledgeReactor, eventProcessorLMAX));
        simpleMessageListenerContainer.setQueues(new Queue("inboundEvent", true, false, false));
        simpleMessageListenerContainer.setExclusive(true);
        simpleMessageListenerContainer.setConsumerArguments(Collections.<String, Object>singletonMap("x-priority", Integer.valueOf(100)));
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleMessageListenerContainer.start();
        return simpleMessageListenerContainer;
    }

    @Bean
    public Channel channel() {
        return cachingConnectionFactory.createConnection().createChannel(false);
    }

    @Bean
    public Exchange exchange() {
        Exchange ex = new TopicExchange("exchange", false, false);
        amqpAdmin.declareExchange(ex);
        return ex;
    }


    private Queue createQueue(String name) {
        Queue q = new Queue(name, false);
        amqpAdmin.declareQueue(q);
        return q;
    }

    @Bean
    @Qualifier("consistentHashExchange")
    public Exchange consistentHashExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("hash-header", "correlationId");
        Exchange ex = new CustomExchange("consistentHashExchange", "x-consistent-hash", false, false, args);

        Binding binding = new Binding(createQueue("bucket1").getName(), Binding.DestinationType.QUEUE, "consistentHashExchange", "1", null);
        amqpAdmin.declareBinding(binding);
        binding = new Binding(createQueue("bucket2").getName(), Binding.DestinationType.QUEUE, "consistentHashExchange", "1", null);
        amqpAdmin.declareBinding(binding);
        binding = new Binding(createQueue("bucket3").getName(), Binding.DestinationType.QUEUE, "consistentHashExchange", "1", null);
        amqpAdmin.declareBinding(binding);

        amqpAdmin.declareExchange(ex);
        return ex;
    }


    /*
    @Bean
    public SimpleMessageListenerContainer consumer2() {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(cachingConnectionFactory);
        simpleMessageListenerContainer.setPrefetchCount(-1);
        simpleMessageListenerContainer.setMessageListener(new ChannelAwareMessageListener(eventLevelReactor));
        simpleMessageListenerContainer.setQueues(new Queue("testQ", true, false, false));
        simpleMessageListenerContainer.setConsumerArguments(Collections.<String, Object>singletonMap("x-priority", Integer.valueOf(20)));
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleMessageListenerContainer.start();
        return simpleMessageListenerContainer;
    }
    */
}

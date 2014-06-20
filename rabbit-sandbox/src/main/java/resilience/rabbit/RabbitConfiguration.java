package resilience.rabbit;


import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.PostConstruct;

@Configuration
@ImportResource("classpath:hazelCast.xml")
public class RabbitConfiguration {

    @Value("#{environment.queueNames}")
    private String queueNames;

    @PostConstruct
    public void setup() {
        System.out.println("init");
        for(String queue: queueNames.split(",")) {
            System.out.println("container for " + queue);
            containerManager().createContainerFor(queue);
        }
    }

    @Bean
    public ContainerManager containerManager() {
        return new ContainerManager();
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        cf.setHost("127.0.0.1");
        cf.setPort(5671);
        cf.setUsername("guest");
        cf.setPassword("guest");
        return new CachingConnectionFactory(cf);
    }


}

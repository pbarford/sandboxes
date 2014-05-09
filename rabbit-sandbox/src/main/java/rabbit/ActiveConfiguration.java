package rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Configuration
@Profile("active")
public class ActiveConfiguration extends RabbitConfiguration {

    @PostConstruct
    public void init() {
        activateBindings();
    }

    private void activateBindings() {
        Binding binding = new Binding("consistentHashExchange", Binding.DestinationType.EXCHANGE, "exchange", "pp", null);
        amqpAdmin.declareBinding(binding);

    }
}

package rabbit;

import model.Event;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.processor.Processor;
import reactor.core.processor.spec.ProcessorSpec;
import reactor.core.spec.Reactors;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;
import reactor.function.Supplier;
import reactor.spring.context.config.EnableReactor;

@Configuration
@EnableReactor
public class ReactorConfiguration {

    @Bean
    public Reactor eventLevelReactor(Environment environment) {
        Reactor eventReactor = Reactors.reactor().env(environment).dispatcher(Environment.RING_BUFFER).get();
        eventReactor.on(Selectors.$(App.EVENT_PROCESSOR_PREFIX + "0"), new EventProcessor(App.EVENT_PROCESSOR_PREFIX + "0", messageAcknowledgeReactor(environment), Reactors.reactor().env(environment).dispatcher(Environment.THREAD_POOL).get()));
        eventReactor.on(Selectors.$(App.EVENT_PROCESSOR_PREFIX + "1"), new EventProcessor(App.EVENT_PROCESSOR_PREFIX + "1", messageAcknowledgeReactor(environment), Reactors.reactor().env(environment).dispatcher(Environment.THREAD_POOL).get()));
        eventReactor.on(Selectors.$(App.EVENT_PROCESSOR_PREFIX + "2"), new EventProcessor(App.EVENT_PROCESSOR_PREFIX + "2", messageAcknowledgeReactor(environment), Reactors.reactor().env(environment).dispatcher(Environment.THREAD_POOL).get()));
        eventReactor.on(Selectors.$(App.EVENT_PROCESSOR_PREFIX + "3"), new EventProcessor(App.EVENT_PROCESSOR_PREFIX + "3", messageAcknowledgeReactor(environment), Reactors.reactor().env(environment).dispatcher(Environment.THREAD_POOL).get()));
        return eventReactor;
    }

    @Bean
    public Reactor messageAcknowledgeReactor(Environment environment) {
        return Reactors.reactor().env(environment).dispatcher(Environment.RING_BUFFER).get();
    }

    @Bean
    public Reactor marketLevelReactor(Environment environment) {
        return Reactors.reactor().env(environment).dispatcher(Environment.THREAD_POOL).get();
    }

    @Bean
    public Processor<Event> eventProcessorLMAX() {
        Consumer<Event> eventProcessorLMAX = new EventProcessorLMAX();
        Supplier<Event> supplier = new Supplier<Event>() {
            @Override
            public Event get() {
                return new Event();
            }
        };
        return new ProcessorSpec<Event>().dataSupplier(supplier).consume(eventProcessorLMAX).get();
    }
}

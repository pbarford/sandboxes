package rabbit;

import com.rabbitmq.client.Channel;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.amqp.core.Message;
import reactor.core.Reactor;
import reactor.core.processor.Operation;
import reactor.core.processor.Processor;
import reactor.event.Event;
import reactor.event.selector.Selectors;

import java.util.HashMap;
import java.util.List;

import static reactor.event.Event.wrap;

public class ChannelAwareMessageListener implements org.springframework.amqp.rabbit.core.ChannelAwareMessageListener {

    private final Reactor eventReactor;
    private final Reactor messageAcknowledgeReactor;
    private final Processor<model.Event> eventProcessorLMAX;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChannelAwareMessageListener(Reactor eventReactor, Reactor messageAcknowledgeReactor, Processor<model.Event> eventProcessorLMAX) {
        this.eventReactor = eventReactor;
        this.messageAcknowledgeReactor = messageAcknowledgeReactor;
        this.eventProcessorLMAX = eventProcessorLMAX;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        registerMessageAcknowlegement(message, channel);
        processMessageInReactor(message);
    }

    private model.Event convertFrom(Message message) throws Exception {
        return mapper.readValue(message.getBody(), model.Event.class);
    }

    private HashMap convertToMap(Message message) throws Exception {
        return mapper.readValue(message.getBody(), HashMap.class);
    }

    private void processMessageInReactor(Message message) throws Exception {
        HashMap jsonMap = convertToMap(message);
        for(Object key : jsonMap.keySet().toArray()) {
            System.out.println(key);
        }
        for(Object key : ((HashMap)jsonMap.get("eventDescriptor")).keySet().toArray()) {
            System.out.println(key);
        }
        System.out.println(((List)jsonMap.get("markets")).get(0).getClass().getName());


        Event<model.Event> event = createEvent(message);
        String selector = App.EVENT_PROCESSOR_PREFIX + (event.getData().getId().hashCode() % 4);
        eventReactor.notify(selector, event);

        invokeProcessorDisruptor(convertFrom(message));
    }

    private void invokeProcessorDisruptor(model.Event event) {
        Operation<model.Event> op = eventProcessorLMAX.prepare();
        model.Event ev = op.get();

        ev.setId(event.getId());
        op.commit();
    }



    private void registerMessageAcknowlegement(Message message, Channel channel) {
        messageAcknowledgeReactor.on(Selectors.$(App.MSG_ACK_SELECTOR + String.valueOf(message.getMessageProperties().getDeliveryTag())),
                                                 new MessageAckProcessor(channel)).cancelAfterUse();
    }

    private Event<model.Event> createEvent(Message message) throws Exception {
        Event<model.Event> event = wrap(convertFrom(message));
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        event.getHeaders().set(App.DELIVERY_TAG, String.valueOf(deliveryTag));
        return event;
    }
}

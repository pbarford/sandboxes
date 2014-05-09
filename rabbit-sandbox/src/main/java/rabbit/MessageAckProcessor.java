package rabbit;

import com.rabbitmq.client.Channel;
import reactor.event.Event;
import reactor.function.Consumer;

import java.io.IOException;

public class MessageAckProcessor implements Consumer<Event<Long>> {

    private final Channel channel;

    public MessageAckProcessor(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void accept(Event<Long> longEvent) {
        try {
            System.out.println("ack - " + longEvent.getData());
            channel.basicAck(longEvent.getData(), false);
            channel.basicPublish("outboundEx", "event", null, "DONE".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

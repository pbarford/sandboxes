package rabbit;

import model.Event;
import reactor.function.Consumer;

public class EventProcessorLMAX implements Consumer<Event> {
    @Override
    public void accept(Event event) {
        System.out.println(Thread.currentThread().getName() + " --> accept : " + event.getId());
    }
}

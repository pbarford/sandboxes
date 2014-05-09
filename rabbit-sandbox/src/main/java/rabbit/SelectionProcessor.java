package rabbit;

import model.Selection;
import reactor.event.Event;
import reactor.function.Consumer;

import java.util.concurrent.CountDownLatch;

public class SelectionProcessor implements Consumer<Event<Selection>> {

    private final CountDownLatch latch;

    public SelectionProcessor(CountDownLatch latch) {
        this.latch = latch;
    }
    @Override
    public void accept(Event<Selection> selectionEvent) {
        latch.countDown();
    }
}

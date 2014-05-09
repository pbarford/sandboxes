package rabbit;

import model.Market;
import model.Selection;
import reactor.event.Event;
import reactor.function.Consumer;

import java.util.concurrent.CountDownLatch;

public class MarketProcessor implements Consumer<Event<Market>> {

    private final CountDownLatch latch;

    public MarketProcessor(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void accept(Event<Market> marketEvent) {
        processInProcess(marketEvent);
    }


    private void processInProcess(Event<Market> marketEvent) {
        for(Selection selection : marketEvent.getData().getSelections()) {
        }
        latch.countDown();
    }
}

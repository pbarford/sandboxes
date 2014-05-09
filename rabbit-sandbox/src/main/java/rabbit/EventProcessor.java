package rabbit;

import reactor.core.Reactor;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

import java.util.concurrent.CountDownLatch;

import static reactor.event.Event.wrap;

public class EventProcessor implements Consumer<Event<model.Event>> {

    private final String id;
    private final Reactor msgAcknowledgeReactor;
    private final Reactor marketLevelReactor;

    public EventProcessor(String id, Reactor msgAcknowledgeReactor, Reactor marketLevelReactor) {
        this.id = id;
        this.msgAcknowledgeReactor = msgAcknowledgeReactor;
        this.marketLevelReactor = marketLevelReactor;
    }

    @Override
    public void accept(Event<model.Event> eventEvent) {
        long inTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(eventEvent.getData().getMarkets().size());
        MarketProcessor marketProcessor = new MarketProcessor(latch);
        String selector = eventEvent.getData().getId().toString();
        Registration reg =  marketLevelReactor.on(Selectors.$(selector), marketProcessor);
        for(model.Market market : eventEvent.getData().getMarkets()) {
            Event<model.Market> marketEvent = wrap(market);
            marketLevelReactor.notify(selector, marketEvent);
        }

        try {
            latch.await();
            acknowledgeMessage(eventEvent);
        } catch (InterruptedException e) {
            latch.notifyAll();
        }
        finally {
            reg.cancel();
        }

        System.out.println(Thread.currentThread().getName() + "--> event process time : " + (System.currentTimeMillis() - inTime) + " using " + id);
    }

    private void acknowledgeMessage(Event<model.Event> eventEvent) {
        String deliveryTag = eventEvent.getHeaders().get(App.DELIVERY_TAG);
        Event<Long> ack = wrap(Long.valueOf(deliveryTag));
        msgAcknowledgeReactor.notify(App.MSG_ACK_SELECTOR + deliveryTag, ack);
    }
}

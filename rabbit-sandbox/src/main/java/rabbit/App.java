package rabbit;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

    public static final String DELIVERY_TAG = "delivery-tag";
    public static final String MSG_ACK_SELECTOR = "msg-ack-";
    public static final String EVENT_PROCESSOR_PREFIX = "event-processor-";

    public static void main( String[] args ) {
        new App();
    }

    public App() {
        new ClassPathXmlApplicationContext("classpath:rabbit.xml");

    }
}

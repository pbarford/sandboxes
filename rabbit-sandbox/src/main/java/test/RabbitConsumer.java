package test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import da.mapper.EventMapper;
import da.model.Event;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.node.Node;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class RabbitConsumer implements ChannelAwareMessageListener {

    private static final String RABBIT_HOST = "10.105.183.30";
    private static final String RABBIT_PORT = "5671";
    private static final String EXCHANGE_NAME = "journalOutbound";
    private static final String QUEUE_NAME = "elasticsearch";
    private static final String INDEX_NAME = "dasearch";

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final EventMapper eventMapper = new EventMapper();
    private final Node node;
    private final ConnectionFactory connectionFactory;
    private final CachingConnectionFactory cachingConnectionFactory;
    private final AmqpAdmin amqpAdmin;

    public static void main(String[] args) {
        try {
            new RabbitConsumer();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public RabbitConsumer() throws KeyManagementException, NoSuchAlgorithmException {
        node = nodeBuilder().node();
        jsonMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        jsonMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(RABBIT_HOST);
        connectionFactory.setPort(Integer.valueOf(RABBIT_PORT).intValue());
        connectionFactory.setUsername("adminPP");
        connectionFactory.setPassword("adm1nPP");
        connectionFactory.useSslProtocol(createSSLContext());
        cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        amqpAdmin = new RabbitAdmin(cachingConnectionFactory);
        bindQueue();
        startConsumer();
    }

    private void startConsumer() {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(cachingConnectionFactory);
        simpleMessageListenerContainer.setPrefetchCount(-1);
        simpleMessageListenerContainer.setMessageListener(this);
        simpleMessageListenerContainer.setQueues(new Queue(QUEUE_NAME, true, false, false));
        simpleMessageListenerContainer.setExclusive(true);
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleMessageListenerContainer.start();
    }

    private Queue createQueue(String name) {
        Queue q = new Queue(name, false);
        amqpAdmin.declareQueue(q);
        return q;
    }

    private void bindQueue() {
        Binding binding = new Binding(createQueue(QUEUE_NAME).getName(), Binding.DestinationType.QUEUE, EXCHANGE_NAME, "", null);
        amqpAdmin.declareBinding(binding);
    }

    private Event convertFrom(byte[] bytes) throws Exception {
        return jsonMapper.readValue(bytes, da.model.Event.class);
    }

    private da.model.Event convertFrom(Message message) throws Exception {
        return convertFrom(message.getBody());
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String id = String.valueOf(message.getMessageProperties().getHeaders().get("eventId"));
        String destination = String.valueOf(message.getMessageProperties().getHeaders().get("destination"));
        System.out.println(id + "-" + destination);
        GetResponse res = node.client()
                              .prepareGet(INDEX_NAME, destination, id)
                              .execute()
                              .actionGet();

        StringBuffer sb = new StringBuffer();
        Event eventUpdate = convertFrom(message);
        if(res.isExists()) {
            String doc = new String(res.getSourceAsBytes());
            System.out.println(doc);
            da.model.Event event = convertFrom(doc.getBytes());
            eventMapper.overlayEventChanges(event, eventUpdate);
            sb.append(jsonMapper.writeValueAsString(event));
            System.out.println("updating doc");
            UpdateResponse ires = node.client().prepareUpdate(INDEX_NAME, destination, id).setDoc(sb.toString()).execute().actionGet();
            System.out.println(ires.isCreated());
        } else {
            System.out.println("indexing new doc");
            sb.append(jsonMapper.writeValueAsString(eventUpdate));
            IndexResponse ires = node.client().prepareIndex(INDEX_NAME, destination, id).setSource(sb.toString()).execute().actionGet();
            System.out.println(ires.isCreated());
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    private SSLContext createSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[] { createTrustManager() }, new SecureRandom());
        return sslContext;
    }

    private TrustManager createTrustManager() {
        return new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers =============");
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkClientTrusted =============");
            }
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkServerTrusted =============");
            }
        };
    }
}

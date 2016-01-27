package test;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import da.model.Event;
import da.mapper.EventMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static com.datastax.driver.core.querybuilder.QueryBuilder.asc;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;

public class Cassandra {

    private final ConnectionFactory rabbitConnectionFactory;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final EventMapper eventMapper = new EventMapper();
    private Cluster cluster;
    private Session session;
    private CloseableHttpClient httpclient = HttpClients.createDefault();

    private static final String INDEX_NAME = "dasearch";

    public static void main(String[] args) {
        new Cassandra();
    }


    public Cassandra() {
        rabbitConnectionFactory = new ConnectionFactory();
        rabbitConnectionFactory.setHost("127.0.0.1");
        rabbitConnectionFactory.setUsername("guest");
        rabbitConnectionFactory.setPassword("guest");

        jsonMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        String[] cipherSuites = { "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA" };

        try {
            Connection connection = rabbitConnectionFactory.newConnection();
            Channel channel = connection.createChannel();
            cluster = Cluster.builder()
                    .addContactPoint("10.105.160.12")
                    .withCredentials("dm_systest", "EmYoqrAwgakHN18FwBbY")
                    .withSSL(new SSLOptions(createSSLContext(), cipherSuites))
                    .build();
            session = cluster.connect("dm_keyspace_systest");
            PreparedStatement statement = session.prepare("select data from outboundmessages where dest=? and eventid=? order by seqno asc");
            Statement statement1 = QueryBuilder.select("data").from("outboundmessages").where(eq("dest", "nike")).and(eq("eventid", 1719826)).orderBy(asc("seqno"));
            //ResultSet rs = session.execute(statement.bind("nike", Integer.valueOf(1719826)));
            ResultSet rs = session.execute(statement1);
            int i=0;
            for(Row row : rs) {
                StringBuffer sb = new StringBuffer();
                if(i==0) {
                    sb.append("{ \"index\" : {\"_id\" : \"1719826\", \"_type\" : \"facts\", \"_index\" : \"" + INDEX_NAME + "\"} }");
                    sb.append("\n");
                    sb.append(row.getString("data"));
                    sb.append("\n");

                    System.out.println(sb.toString());
                    //channel.basicPublish("elasticsearch", "elasticsearch", null, sb.toString().getBytes());
                }
                else {
                    Event event = jsonMapper.readValue(row.getString("data"), da.model.Event.class);
                    processEventChange(event, channel);
                }
                i++;
            }

            session.shutdown();
            cluster.shutdown();

            channel.close();
            connection.close();
            httpclient.close();
        }
        catch(Exception ex) {}
    }

    private void processEventChange(Event eventUpdate, Channel channel) {
        StringBuffer sb = new StringBuffer();
        HttpGet get = new HttpGet("http://localhost:9200/" + INDEX_NAME + "/facts/1719826");
        try {
            CloseableHttpResponse response1 = httpclient.execute(get);
            String res = EntityUtils.toString(response1.getEntity());
            response1.close();
            String[] parts = res.split("\"_source\":");
            //System.out.println(parts[1].substring(0, parts[1].length() - 1));
            Event event = jsonMapper.readValue(parts[1].substring(0, parts[1].length() - 1), da.model.Event.class);
            eventMapper.overlayEventChanges(event, eventUpdate);
            sb.append("{ \"index\" : {\"_id\" : \"1719826\", \"_type\" : \"facts\", \"_index\" : \"" + INDEX_NAME + "\"} }");
            sb.append("\n");
            sb.append(jsonMapper.writeValueAsString(event));
            sb.append("\n");
            channel.basicPublish("elasticsearch", "elasticsearch", null, sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

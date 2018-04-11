package examples.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public abstract class Producer {
    protected String topic;
    protected KafkaProducer<String, String> producer = getProducer();


    KafkaProducer<String, String> getProducer(){
        Properties config = new Properties();
        config.put("client.id", "my_client_id");
        config.put("bootstrap.servers", "kylin-test.0303041005.zbj:6667,kylin-test.0303041004.zbj:6667");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put("acks", "all");
        KafkaProducer<String,String> producer =new KafkaProducer<String, String>(config);
        return producer;
    }

     abstract void run();

}

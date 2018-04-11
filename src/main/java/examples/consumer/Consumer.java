package examples.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public abstract class Consumer {
    protected KafkaConsumer<String, String> consumer = getConsumer();
    private static final String brokerList= "kylin-test.0303041005.zbj:6667,kylin-test.0303041004.zbj:6667";

    protected KafkaConsumer<String, String> getConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");//不自动提交offset
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");//配合不自动提交offset使用，使得consumer从最早的数据开始读取
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(props);

    }

    abstract void run();
}

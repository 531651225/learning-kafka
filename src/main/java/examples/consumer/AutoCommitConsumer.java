package examples.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class AutoCommitConsumer extends Consumer{
    private String topic;

    AutoCommitConsumer(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka";
        AutoCommitConsumer consumer = new AutoCommitConsumer(topic);
        consumer.run();
    }

    void run(){
        KafkaConsumer<String, String> consumer = getConsumer();
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            System.out.println("poll returned " + records.count() + " records");
            for (ConsumerRecord<String, String> record : records) {
                processData(record);
            }
        }
    }
    @Override
    protected KafkaConsumer<String, String> getConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kylin-test.0303041005.zbj:6667,kylin-test.0303041004.zbj:6667");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");//不自动提交offset
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");//配合不自动提交offset使用，使得consumer从最早的数据开始读取
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(props);

    }
}

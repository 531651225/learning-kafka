package examples.consumer.avro;

import JavaSessionize.avro.LogLine;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

public class AvroClicksConsumer {
    private final KafkaConsumer<String, GenericRecord> consumer;
    private final String brokerList;
    private final String schemaRegistryUrl;
    private final String inputTopic;

    public static void main(String[] args) {

        String inputTopic = "clicks";
        String url = "http://slave1:8081";
        String brokerList = "slave2:9092";
        AvroClicksConsumer clickConsumer = new AvroClicksConsumer(brokerList, inputTopic, url);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> clickConsumer.consumer.close()));
        clickConsumer.run();
    }

    public AvroClicksConsumer(String brokerList, String inputTopic, String url) {
        this.inputTopic = inputTopic;
        this.schemaRegistryUrl = url;
        this.brokerList = brokerList;
        this.consumer = getConsumer();
    }


    private void run() {
        KafkaConsumer<String, GenericRecord> consumer = getConsumer();
        consumer.subscribe(Arrays.asList(this.inputTopic));
        while (true) {
            ConsumerRecords<String, GenericRecord> records = consumer.poll(100);
            System.out.println("poll returned " + records.count() + " records");
            for (ConsumerRecord<String, GenericRecord> record : records) {
                String ip = record.key();
                GenericRecord genericEvent = record.value();
                LogLine event = (LogLine) SpecificData.get().deepCopy(LogLine.SCHEMA$, genericEvent);
                System.out.println("ip:" + ip);
                System.out.println("log:" + event);
            }
        }
    }

    private KafkaConsumer<String, GenericRecord> getConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");//不自动提交offset
        props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");//配合不自动提交offset使用，使得consumer从最早的数据开始读取
        props.put("key.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        return new KafkaConsumer<>(props);
    }
}
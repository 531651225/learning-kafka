package examples.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Arrays;

public class AsyncConsumer extends Consumer {
    private String topic;

    AsyncConsumer(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka*";//regex
        AsyncConsumer consumer = new AsyncConsumer(topic);
        consumer.run();
    }

    void run(){
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            System.out.println("poll returned " + records.count() + " records");
            for (ConsumerRecord<String, String> record : records) {
                consumer.commitAsync();
                System.out.println(record.value());
            }
        }
    }
}

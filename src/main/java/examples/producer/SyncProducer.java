package examples.producer;

import com.alibaba.fastjson.JSON;
import examples.data.Data;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SyncProducer extends Producer {
    SyncProducer(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka";
        final SyncProducer producer = new SyncProducer(topic);
        producer.run();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> producer.producer.close()));
    }

     void run() {
        try {
            while (true) {
                Data data = new Data();
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, null, JSON.toJSON(data).toString());
                //不阻塞于future.get()即可认为是异步发送
                Future<RecordMetadata> future = producer.send(record);
                RecordMetadata metadata = null;
                metadata = future.get();
                System.out.printf("send success for offset %s\n",metadata);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            producer.close();
        }
    }

}

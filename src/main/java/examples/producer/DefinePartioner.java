package examples.producer;

import com.alibaba.fastjson.JSON;
import examples.data.Data;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DefinePartioner extends Producer {
    DefinePartioner(String topic){
        this.topic = topic;
        this.producer = getProducer();
    }
    public static void main(String[] args){
        String topic = "testkafka";
        final DefinePartioner producer = new DefinePartioner(topic);
        producer.run();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> producer.producer.close()));
    }

     void run() {
        KafkaProducer<String, String> producer = getProducer();
        try {
            while (true) {
                Data data = new Data();
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, ""+new Random().nextInt(100), JSON.toJSON(data).toString());
                Future<RecordMetadata> future = producer.send(record);

                RecordMetadata metadata = metadata = future.get();

                System.out.println(metadata.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            producer.close();
        }
    }


    @Override
    KafkaProducer<String, String> getProducer(){
        Properties config = new Properties();
        config.put("client.id", "my_client_id");
        config.put("bootstrap.servers", "kylin-test.0303041005.zbj:6667,kylin-test.0303041004.zbj:6667");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put("acks", "all");
        //设置自定义分区
        config.put("partitioner.class", "examples.producer.TestPartitioner");
        KafkaProducer<String,String> producer =new KafkaProducer<String, String>(config);
        return producer;
    }
}


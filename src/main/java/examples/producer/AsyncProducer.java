package examples.producer;

import com.alibaba.fastjson.JSON;
import examples.data.Data;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class AsyncProducer extends Producer {
    AsyncProducer(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testconsumer";
        final AsyncProducer producer = new AsyncProducer(topic);
        producer.run();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> producer.producer.close()));
    }

    void run() {
            while (true) {
                Data data = new Data();
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, null, JSON.toJSON(data).toString());
                producer.send(record,new RecordCallback());
            }
    }

}
//异步发送消息,同事对异常情况进行处理
class RecordCallback implements Callback{

    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if(null != e){
            e.printStackTrace();
            System.out.println(recordMetadata.toString());
        }else {
            System.out.println(recordMetadata.toString());
        }
    }
}
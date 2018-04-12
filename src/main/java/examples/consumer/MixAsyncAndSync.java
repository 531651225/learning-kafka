package examples.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;

public class MixAsyncAndSync extends Consumer{
    private String topic;

    MixAsyncAndSync(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka";//regex
        MixAsyncAndSync consumer = new MixAsyncAndSync(topic);
        Runtime.getRuntime().addShutdownHook(new Thread(()->consumer.consumer.close()));
        consumer.run();
    }

    void run(){
        consumer.subscribe(Arrays.asList(topic));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                System.out.println("poll returned " + records.count() + " records");
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic=%s,partion=%s,offset=%d\n", record.topic(), record.partition(), record.offset());
                }
                consumer.commitAsync();

            }
        }catch (WakeupException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ////只有try/finally，表示不捕获异常，异常自动向上传递，但finally中的代码在异常发生后也执行。
            try {
                //commit失败是不可恢复的错误。在此结束
                consumer.commitSync();
            }finally {
                consumer.close();
            }
        }
    }
}

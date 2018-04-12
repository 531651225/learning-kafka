package examples.consumer;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指定偏移量提交并指定在再均衡监听器
 */
public class UserDefineOffsetCommit extends Consumer{
    private String topic;
    private Map<TopicPartition,OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();

    UserDefineOffsetCommit(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka";//regex
        UserDefineOffsetCommit consumer = new UserDefineOffsetCommit(topic);
        Runtime.getRuntime().addShutdownHook(new Thread(()->consumer.consumer.close()));
        consumer.run();
    }

    /**
     * onPartitionsRevoked 会在在均衡开始之前和消费者停止读取消息之后调用
     * onPartitionsAssigned 会在重新分配分区之后,消费者开始读取消息之前调用
     */
    private class HandleRebalance implements ConsumerRebalanceListener{

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            System.out.println("lost partitions in rebalance commit offset: "+currentOffsets);
            consumer.commitSync(currentOffsets);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

        }
    }





   void run(){
        try {


            consumer.subscribe(Arrays.asList(topic), new HandleRebalance());
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                System.out.println("poll returned " + records.count() + " records");
                for (ConsumerRecord<String, String> record : records) {
                    //处理数据
                    System.out.printf("topic=%s,partion=%s,offset=%d\n", record.topic(), record.partition(), record.offset());
                    //写入最新的offset
                    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1, ""));
                }
                consumer.commitAsync(currentOffsets, null);

            }
        }catch (WakeupException e){
            //忽略一次。正在关闭消费者
        }catch (Exception e){
            System.out.print("unexpect error");
            e.printStackTrace();
        }finally {
            try {
                consumer.commitSync(currentOffsets);
            }finally {
                consumer.close();
            }
        }
    }
}

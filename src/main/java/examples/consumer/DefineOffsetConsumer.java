package examples.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefineOffsetConsumer extends Consumer{
    private String topic;
    private Map<Integer,Long> currentOffsets = new ConcurrentHashMap<>();


    DefineOffsetConsumer(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka";//regex
        DefineOffsetConsumer consumer = new DefineOffsetConsumer(topic);
        Runtime.getRuntime().addShutdownHook(new Thread(()->consumer.consumer.close()));
        consumer.run();
    }

    void run(){
        consumer.subscribe(Arrays.asList(topic),new HandleRebalance());
        consumer.poll(0);//加入到消费者群组里,第一次调用poll
        for(TopicPartition topicPartition : consumer.assignment()){
            consumer.seek(topicPartition,getOffsetFromDB(topicPartition));
        }
        while (true) {
            //poll维护两个offset,consumer offset 和commit offset
            ConsumerRecords<String, String> records = consumer.poll(100);
            System.out.println("poll returned " + records.count() + " records");
            for (ConsumerRecord<String, String> record : records) {
                processData(record);
                storeData(record);
                storeOffsetInDB(record);
            }
            //同时提交offset和数据的事务,要么都成功要么都失败
            commitDBTransaction();

        }
    }

    private long getOffsetFromDB(TopicPartition partition){
        Long offset = currentOffsets.get(partition);
        if(null != offset){
            return offset;
        }
        return 1;
    }
    /**
     * 提交事务,保证offset和数据的事务
     */
    private void commitDBTransaction(){

    }

    /**
     * 处理数据
     */
    private void processData(ConsumerRecord record){
        System.out.printf("topic=%s,partion=%s,offset=%d\n", record.topic(), record.partition(), record.offset());
    }

    /**
     * 存储offset
     */
    private void storeOffsetInDB(ConsumerRecord record){
        currentOffsets.put(record.partition(),record.offset() + 1);

    }
    /**
     * 存储数据
     */
    private void storeData(ConsumerRecord record){

    }

    /**
     * onPartitionsRevoked 会在在均衡开始之前和消费者停止读取消息之后调用
     * onPartitionsAssigned 会在重新分配分区之后,消费者开始读取消息之前调用
     */
    private class HandleRebalance implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            commitDBTransaction();
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            for(TopicPartition partition: partitions){
                System.out.println("seek??????????");
                consumer.seek(partition,getOffsetFromDB(partition));
            }

        }
    }
}

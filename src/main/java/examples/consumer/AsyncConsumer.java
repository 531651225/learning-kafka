package examples.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class AsyncConsumer extends Consumer {
    private String topic;

    AsyncConsumer(String topic){
        this.topic = topic;
    }
    public static void main(String[] args){
        String topic = "testkafka";//regex
        AsyncConsumer consumer = new AsyncConsumer(topic);
        Runtime.getRuntime().addShutdownHook(new Thread(()->consumer.consumer.close()));
        consumer.run();
    }

    void run(){
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            System.out.println("poll returned " + records.count() + " records");
            for (ConsumerRecord<String, String> record : records) {
                processData(record);
            }
            consumer.commitAsync(new OffsetCommitCallback() {
                @Override
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offset, Exception e) {
                    if(null != e){
                        e.printStackTrace();
                        System.out.printf("commit fail for offset %s",offset);
                    }
                    System.out.printf("commit success for offset %s",offset);

                }
            });

        }
    }

    /**
     * onPartitionsRevoked 会在在均衡开始之前和消费者停止读取消息之后调用
     * onPartitionsAssigned 会在重新分配分区之后,消费者开始读取消息之前调用
     */
    private class HandleRebalance implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
//            System.out.println("lost partitions in rebalance commit offset: "+currentOffsets);
//            consumer.commitSync(currentOffsets);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

        }
    }
}

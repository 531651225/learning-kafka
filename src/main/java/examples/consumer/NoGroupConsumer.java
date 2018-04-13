package examples.consumer;

import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.ArrayList;
import java.util.List;

/**
 * 独立消费者 不需要消费者群组和再均衡
 * 适用于 只需要一个消费者从一个主题的指定分区消费数据
 */
public class NoGroupConsumer extends Consumer {
    private String topic;

    NoGroupConsumer(String topic) {
        this.topic = topic;
    }

    public static void main(String[] args) {
        String topic = "testkafka";//regex
        NoGroupConsumer consumer = new NoGroupConsumer(topic);
        consumer.run();
    }

    void run() {
        List<TopicPartition> partitions = new ArrayList<>();
        List<PartitionInfo> partitionList = consumer.partitionsFor(topic);

        if (partitionList != null) {
            PartitionInfo firstPartion = partitionList.get(0);
            partitions.add(new TopicPartition(firstPartion.topic(), firstPartion.partition()));
        }

        consumer.assign(partitions);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                System.out.println("poll returned " + records.count() + " records");
                for (ConsumerRecord<String, String> record : records) {
                    processData(record);
                }
                //处理完所以记录 再提交.程序
                doCommitSync();
            }
        } catch (WakeupException e) {
            e.printStackTrace();
        } catch (CommitFailedException e) {
            e.printStackTrace();
        }
    }

    void doCommitSync() {
        try {
            consumer.commitSync();
        } catch (WakeupException e) {
            //此时捕获`WakeupException`代表在`commitSync()`过程中有别的线程想要关闭consumer。
            //因此，再调用一次`doCommmitSync()`，确定offset被提交，然后再抛出WakeupException到外层的while循环
            //以使得程序可以退出
            doCommitSync();
            throw e;
        } catch (CommitFailedException e) {
            e.printStackTrace();
            // commit失败是不可恢复的错误。如果有其它的内部状态依赖于commit，应该在这里进行处理。
            //log.debug("Commit failed", e);
        }
    }
}
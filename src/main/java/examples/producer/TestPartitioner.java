package examples.producer;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.record.InvalidRecordException;
import org.apache.kafka.common.utils.Utils;

import java.util.List;
import java.util.Map;

public class TestPartitioner implements Partitioner{
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        List<PartitionInfo> partitionInfos = cluster.partitionsForTopic(topic);
        int size = partitionInfos.size();
        if(null == key){
            throw new InvalidRecordException("key must not be null");
        }
        if(((String)key).equals("1")){
            //写最后分区
            return size-1;
        }
        //其余的写其他分区中
        return (Math.abs(Utils.murmur2(keyBytes)%(size-1)));
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}

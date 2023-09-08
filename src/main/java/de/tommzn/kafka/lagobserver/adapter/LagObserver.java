package de.tommzn.kafka.lagobserver.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.TopicPartition;

public interface LagObserver {
    
    public Map<TopicPartition, Long> getConsumerGroupOffsets(String groupId) throws ExecutionException, InterruptedException;
    
    public Map<TopicPartition, Long> getProducerOffsets(Set<TopicPartition> topicPartitions);
    
    public List<String> getConsumerGroupIds() throws ExecutionException, InterruptedException;

}

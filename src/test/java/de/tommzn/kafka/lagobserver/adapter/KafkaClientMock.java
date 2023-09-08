package de.tommzn.kafka.lagobserver.adapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;

public class KafkaClientMock  implements LagObserver {
    
    @Value("${lagobserver.simulation.topic.name}")
    private String topic;
    
    public Map<TopicPartition, Long> getConsumerGroupOffsets(String groupId) throws ExecutionException, InterruptedException {
        Map<TopicPartition, Long> consumerGroupOffsets = new HashMap<>();
        consumerGroupOffsets.put(new TopicPartition(topic, 0), 3L);
        consumerGroupOffsets.put(new TopicPartition(topic, 1), 1L);
        return consumerGroupOffsets;
    }
    
    public Map<TopicPartition, Long> getProducerOffsets(Set<TopicPartition> topicPartitions) {
        Map<TopicPartition, Long> producerOffsets = new HashMap<>();
        producerOffsets.put(new TopicPartition(topic, 0), 10L);
        producerOffsets.put(new TopicPartition(topic, 1), 3L);
        return producerOffsets;
    }

    public List<String> getConsumerGroupIds() throws ExecutionException, InterruptedException {
        return Arrays.asList("group1", "group2");
    }
}
package de.tommzn.kafka.lagobserver.adapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
 
public class KafkaClient implements LagObserver {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClient.class);
    
    @Autowired
    private AdminClient adminClient;
    
    @Autowired
    private KafkaConsumer<String, String> consumer;
    
    public Map<TopicPartition, Long> getConsumerGroupOffsets(String groupId) throws ExecutionException, InterruptedException {
        
        ListConsumerGroupOffsetsResult info = adminClient.listConsumerGroupOffsets(groupId);
        Map<TopicPartition, OffsetAndMetadata> metadataMap = info.partitionsToOffsetAndMetadata().get();
        Map<TopicPartition, Long> groupOffset = new HashMap<>();

        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : metadataMap.entrySet()) {
            TopicPartition key = entry.getKey();
            OffsetAndMetadata metadata = entry.getValue();
            groupOffset.putIfAbsent(new TopicPartition(key.topic(), key.partition()), metadata.offset());
        }
        return groupOffset;
    }
    
    public Map<TopicPartition, Long> getProducerOffsets(Set<TopicPartition> topicPartitions) { 
        return consumer.endOffsets(new LinkedList<TopicPartition>(topicPartitions));
    }

    public List<String> getConsumerGroupIds() throws ExecutionException, InterruptedException {
        return adminClient.listConsumerGroups().all().get().stream()
                .map( consumerGroupListing -> { return consumerGroupListing.groupId(); })
                .collect(Collectors.toList());
    }
}
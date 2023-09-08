package de.tommzn.kafka.lagobserver.service;

import de.tommzn.kafka.lagobserver.adapter.LagObserver;
import de.tommzn.kafka.lagobserver.model.ConsumerLagRecord;
import de.tommzn.kafka.lagobserver.persistence.ConsumerLagRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class LagAnalyzerService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LagAnalyzerService.class);

    @Autowired
    protected LagObserver kafkaClient;
    
    @Autowired
    ConsumerLagRepository repository;
    
    /*
    public void setLagObserver(LagObserver kafkaClient) {
        this.kafkaClient = kafkaClient;    
    }
    */
   
    public Map<TopicPartition, Long> analyzeLag(String groupId) throws ExecutionException, InterruptedException {
        
        LOGGER.info("Request consumer offsets for groupId {}", groupId);
        Map<TopicPartition, Long> consumerGrpOffsets = kafkaClient.getConsumerGroupOffsets(groupId);
        LOGGER.info("Get {} consumer offsets for groupId {}", consumerGrpOffsets.size(), groupId);
        for (Map.Entry<TopicPartition, Long> lagEntry : consumerGrpOffsets.entrySet()) {
            String topic = lagEntry.getKey().topic();
            int partition = lagEntry.getKey().partition();
            Long offset = lagEntry.getValue();
            LOGGER.info("ConsumerOffset for topic = {}, partition = {}, groupId = {} is {}", topic, partition, groupId, offset);
        }
        
        Map<TopicPartition, Long> producerOffsets = kafkaClient.getProducerOffsets(consumerGrpOffsets.keySet());
        for (Map.Entry<TopicPartition, Long> lagEntry : producerOffsets.entrySet()) {
            String topic = lagEntry.getKey().topic();
            int partition = lagEntry.getKey().partition();
            Long offset = lagEntry.getValue();
            LOGGER.info("ProducerOffset for topic = {}, partition = {}, groupId = {} is {}", topic, partition, groupId, offset);
        }
        
        Map<TopicPartition, Long> lags = computeLags(consumerGrpOffsets, producerOffsets);
        for (Map.Entry<TopicPartition, Long> lagEntry : lags.entrySet()) {
            String topic = lagEntry.getKey().topic();
            int partition = lagEntry.getKey().partition();
            Long lag = lagEntry.getValue();
            LOGGER.info("Lag for topic = {}, partition = {}, groupId = {} is {}", topic, partition, groupId, lag);
        }
        persistConsumerLag(groupId, lags);
        return lags;
    }
    
    public Map<TopicPartition, Long> computeLags(Map<TopicPartition, Long> consumerGrpOffsets, Map<TopicPartition, Long> producerOffsets) {
      
        Map<TopicPartition, Long> lags = new HashMap<>();
        for (Map.Entry<TopicPartition, Long> entry : consumerGrpOffsets.entrySet()) {
            if (consumerGrpOffsets.containsKey(entry.getKey()) && producerOffsets.containsKey(entry.getKey())) {
                Long producerOffset = producerOffsets.get(entry.getKey());
                Long consumerOffset = consumerGrpOffsets.get(entry.getKey());
                long lag = Math.abs(Math.max(0, producerOffset) - Math.max(0, consumerOffset));
                lags.putIfAbsent(entry.getKey(), lag);    
            }
        }
        return lags;
    }

    private void persistConsumerLag(String groupId, Map<TopicPartition, Long> consumerLags) {
        for (Map.Entry<TopicPartition, Long> entry : consumerLags.entrySet()) {
            repository.save(new ConsumerLagRecord(groupId, entry.getKey().topic(), entry.getKey().partition(), entry.getValue()));
        }
    }
}
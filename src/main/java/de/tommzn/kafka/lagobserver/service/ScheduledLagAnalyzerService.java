package de.tommzn.kafka.lagobserver.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
public class ScheduledLagAnalyzerService extends LagAnalyzerService implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledLagAnalyzerService.class);
    
    @Value("#{'${lagobserver.kafka.consumer.groupid}'.split(',')}")
    private List<String> groupIds;

    private Map<Integer, Long> metrics = new HashMap<>();
    
    @Override
    public void afterPropertiesSet() {
        if (groupIds.size() == 0) {
            try {
                groupIds = kafkaClient.getConsumerGroupIds();
            } catch (Exception e) {
               LOGGER.error("Unable to get consumer group ids, reason {}", e.getMessage()); 
            } 
        }
    }   

    @Scheduled(fixedDelay = 5000L)
    public void liveLagAnalysis() throws ExecutionException, InterruptedException {
        
        LOGGER.info("Analyze Consumer Lags");
        
        for (String groupId : groupIds) {

            LOGGER.info("Analyze GroupId: "+ groupId);    
            Map<TopicPartition, Long> consumerLags = this.analyzeLag(groupId);
            for (Map.Entry<TopicPartition, Long> lagEntry : consumerLags.entrySet()) {
                updateConsumerLagMetrics(groupId, lagEntry.getKey(), lagEntry.getValue());
            }
        }
    }
    
    private void updateConsumerLagMetrics(String groupId, TopicPartition topicPartition, Long lag) {
        
        int hash = Arrays.hashCode(new String[]{ groupId, topicPartition.topic(), String.valueOf(topicPartition.partition())});
        
        if (!metrics.containsKey(hash)) {
            Tags tags = Tags.of("topic", topicPartition.topic(), "partition", String.valueOf(topicPartition.partition()), "groupid", groupId);
            Gauge.builder("consumer.lag", metrics, map -> map.get(hash))
                    .tags(tags)
                    .register(Metrics.globalRegistry);
        }
        metrics.put(hash, lag);
    }
}
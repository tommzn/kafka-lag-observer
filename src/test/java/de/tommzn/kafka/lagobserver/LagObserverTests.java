package de.tommzn.kafka.lagobserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.tommzn.kafka.lagobserver.adapter.KafkaClientMock;
import de.tommzn.kafka.lagobserver.adapter.LagObserver;
import de.tommzn.kafka.lagobserver.service.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(value = "test")
class LagObserverTests {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LagObserverTests.class);
    
    @Autowired
    private LagAnalyzerService lagAnalyzer;
    
    @Value("${lagobserver.simulation.consumer.groupid}")
    private String groupId;
    
    @TestConfiguration
    static class LagObserverTestsConfig {
        
        @Primary
        @Bean
        public LagObserver kafkaClient() {
            return new KafkaClientMock();
        }
    }
    
    @Test
    public void analyze_lags() {
        
        try{
    	    
    	    Map<TopicPartition, Long> poartitionLags = lagAnalyzer.analyzeLag(groupId);
    	    assertTrue(poartitionLags.size() > 0);
        	for (Map.Entry<TopicPartition, Long> lagEntry : poartitionLags.entrySet()) {
        	    LOGGER.info("analyze_lags, topic = {}, partition = {}, groupId = {} is {}", lagEntry.getKey().topic(), lagEntry.getKey().partition(), groupId, lagEntry.getValue());
                assertTrue(lagEntry.getValue() > 0);
            }    
        } catch(Exception e) {
            LOGGER.info("analyze_lags failed, reason: {}", e.getMessage());
            fail(e.getMessage());
        }
    }
    
    @Test
    public void calculate_lag() {
        
        TopicPartition topicPartition0 = new TopicPartition("topic01", 0);
        TopicPartition topicPartition1 = new TopicPartition("topic01", 1);
        
        Map<TopicPartition, Long> consumerOffsets = new HashMap<>();
        consumerOffsets.put(topicPartition0, 10L);
        consumerOffsets.put(topicPartition1, 12L);
        consumerOffsets.put(new TopicPartition("topic02", 0), 5L);
        
        Map<TopicPartition, Long> producerOffsets = new HashMap<>();
        producerOffsets.put(topicPartition0, 15L);
        producerOffsets.put(topicPartition1, 19L);
        producerOffsets.put(new TopicPartition("topic03", 0), 25L);
        
        Map<TopicPartition, Long> computedlags = lagAnalyzer.computeLags(consumerOffsets, producerOffsets);
        
        assertEquals(2, computedlags.size());
        assertTrue(computedlags.containsKey(topicPartition0));
        assertTrue(computedlags.containsKey(topicPartition1));
        assertEquals(5L, computedlags.get(topicPartition0));
        assertEquals(7L, computedlags.get(topicPartition1));
    }
}

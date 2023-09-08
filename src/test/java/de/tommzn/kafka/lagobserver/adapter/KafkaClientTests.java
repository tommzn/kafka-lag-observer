package de.tommzn.kafka.lagobserver.adapter;

import de.tommzn.kafka.lagobserver.simulation.*;
import de.tommzn.kafka.lagobserver.model.*;
import de.tommzn.kafka.lagobserver.adapter.KafkaClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.common.TopicPartition;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Testcontainers
@SpringBootTest
@ActiveProfiles(value = "test")
class KafkaClientTests {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClientTests.class);

    @Container
    private static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.2"));
    
    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

	@Autowired
    private ProducerSimulator producerSimulator;
    
    @Autowired
    private ConsumerSimulator consumerSimulator;
    
    @Autowired
    private KafkaClient kafkaClient;
    
    @Value("${lagobserver.simulation.consumer.groupid}")
    private String groupId;
    
    @Value("${lagobserver.simulation.topic.name}")
    private String topic;
    
    @BeforeEach
    public void setupTest() {
        await().atMost(30, SECONDS).until(() -> kafkaContainer.isRunning() );
        consumerSimulator.resetMessages();
    }
    
    @Test
    public void get_consumer_offset() {
        
        publishMessages("get_consumer_offset", 3);
    	
    	Map<TopicPartition, Long> groupOffsets = new HashMap<>();
    	try{
    	    groupOffsets = kafkaClient.getConsumerGroupOffsets(groupId);
        } catch(Exception e) {
            LOGGER.info("get_consumer_offset failed, reason: {}", e.getMessage());
            fail(e.getMessage());
        }
        assertTrue(groupOffsets.size() > 0);
        for (Long offset : groupOffsets.values()) {
            assertTrue(offset > 0);
        }
    }
    
    @Test
    public void get_producer_offset() {
        
        publishMessages("get_producer_offset", 3);
        
    	Set<TopicPartition> topicPartitions = new HashSet<>();
    	topicPartitions.add(new TopicPartition(topic, 0));
    	try{
    	    
    	    Map<TopicPartition, Long> producerOffsets = kafkaClient.getProducerOffsets(topicPartitions);
    	    assertTrue(producerOffsets.size() > 0);
            for (Map.Entry<TopicPartition, Long> offsetEntry : producerOffsets.entrySet()) {
                LOGGER.info("Producer offset:  topic: {}, partition: {}, offset: {}", offsetEntry.getKey().topic(), offsetEntry.getKey().partition(), offsetEntry.getValue());
                assertTrue(offsetEntry.getValue() > 0);
            }
            
        } catch(Exception e) {
            LOGGER.info("get_producer_offset failed, reason: {}", e.getMessage());
            fail(e.getMessage());
        }
    	
    }

    @Test
    public void get_consumer_groupids() {

        try {
            List<String> groupIds = kafkaClient.getConsumerGroupIds();
            assertTrue(groupIds.size() > 0);
        } catch(Exception e) {
            LOGGER.info("get_producer_offset failed, reason: {}", e.getMessage());
            fail(e.getMessage());
        }
    }
    
    private void publishMessages(String message, int messageCount) {
        for (int i = 0; i < messageCount; i++) {
            assertTrue(producerSimulator.send(message + " : " + Instant.now().toString()));
        }    
    }

}

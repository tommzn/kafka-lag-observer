package de.tommzn.kafka.lagobserver;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import de.tommzn.kafka.lagobserver.model.*;
import de.tommzn.kafka.lagobserver.simulation.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(classes = LagObserverApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = "integration")
class ConsumerLagControllerTests {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerLagControllerTests.class);
    
    @Container
    private static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.2"));
    
    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }
    
    @LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;
	
	@Autowired
    private ProducerSimulator producerSimulator;
    
    @Autowired
    private ConsumerSimulator consumerSimulator;
    
    @Value("${lagobserver.simulation.consumer.groupid}")
    private String groupId;
    
    @Value("${lagobserver.simulation.topic.name}")
    private String topic;
    
    @Test
    public void publish_message_and_get_consumer_lag() {
    	
        String healthEndPoint      = "http://localhost:" + port + "/actuator/health";
        String consumerLagEndPoint = "http://localhost:" + port + "/consumerlag";

        await().atMost(30, SECONDS).until(() -> assert_running_app(healthEndPoint));
        await().atMost(30, SECONDS).until(() -> kafkaContainer.isRunning() );
		
        publishMessages(3);
        await().atMost(30, SECONDS).until(() -> consumerSimulator.getMessageCount() >= 3 );
		await().atMost(30, SECONDS).until(() -> assert_consumer_lag(consumerLagEndPoint) );

		ConsumerLag[] consumerLags = fetch_consumer_lag(consumerLagEndPoint);
        assertEquals(1, consumerLags.length);
		assertEquals(groupId, consumerLags[0].getGroupid());
		assertEquals(topic, consumerLags[0].getTopic());
		assertEquals(0, consumerLags[0].getPartition());
		assertEquals(0, consumerLags[0].getLag());
		
    }
    
    private boolean assert_running_app(final String endPoint) {
        final ResponseEntity<String> response = restTemplate.getForEntity(endPoint, String.class);
        return HttpStatus.OK == response.getStatusCode();
    }

    private boolean assert_consumer_lag(String endPoint) {
        ConsumerLag[] consumerLags = fetch_consumer_lag(endPoint);
        return consumerLags.length > 0;
    }
    
    private ConsumerLag[] fetch_consumer_lag(final String endPoint) {     
        final ResponseEntity<ConsumerLag[]> response = restTemplate.getForEntity(endPoint, ConsumerLag[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }
    
    private void publishMessages(int messageCount) {
        for (int i = 0; i < messageCount; i++) {
            producerSimulator.send("Message: " + Instant.now().toString());
        }
    }
}

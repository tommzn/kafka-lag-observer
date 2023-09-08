package de.tommzn.kafka.lagobserver.simulation;

import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

public class ProducerSimulator {

    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${lagobserver.simulation.topic.name}")
    private String topicName;
    
    @Autowired
    public ProducerSimulator(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public boolean send(String message) {
        CompletableFuture<SendResult<String, String>> completable = kafkaTemplate.send(topicName, message);
        try {
            completable.get();
            return completable.isDone() && !completable.isCancelled() && !completable.isCompletedExceptionally();
        } catch (Exception e) {
            return false;
        }
    }
}
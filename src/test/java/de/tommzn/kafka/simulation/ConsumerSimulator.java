package de.tommzn.kafka.lagobserver.simulation;

import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerSimulator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerSimulator.class);
    
    ArrayList<String> messages = new ArrayList<String>();
    
    @KafkaListener(topics = "${lagobserver.simulation.topic.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listenGroup(String message) throws InterruptedException {
        LOGGER.info("Receive message: {}", message);
        messages.add(message);
    }
    
    public int getMessageCount() {
        return messages.size();
    }

    public void resetMessages() {
        messages = new ArrayList<String>();
    }
}
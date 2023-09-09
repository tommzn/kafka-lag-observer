package de.tommzn.kafka.lagobserver.simulation;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class KafkaProducerConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 500);
        configProps.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 100);
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerSimulator producerSimulator() {
        return new ProducerSimulator(kafkaTemplate());
    }
}


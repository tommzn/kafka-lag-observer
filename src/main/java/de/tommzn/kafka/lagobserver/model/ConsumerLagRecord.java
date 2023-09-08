package de.tommzn.kafka.lagobserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class ConsumerLagRecord {
    
    @Id
    @GeneratedValue
    private Long id;

    private String groupId;
    
    private String topic;
    
    private int partition;
    
    private Long lag;
    
    public ConsumerLagRecord(String groupId, String topic, int partition, Long lag) {
        this.groupId    = groupId;
        this.topic      = topic;
        this.partition  = partition;
        this.lag        = lag;
    }
}
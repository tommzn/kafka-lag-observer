package de.tommzn.kafka.lagobserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConsumerLag {
    
    private String groupid;
    
    private String topic;
    
    private int partition;
    
    private Long lag;
    
}
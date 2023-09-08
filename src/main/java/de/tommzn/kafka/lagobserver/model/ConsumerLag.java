package de.tommzn.kafka.lagobserver.model;

import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class ConsumerLag {
    
    private String groupid;
    
    private String topic;
    
    private int partition;
    
    private Long lag;
    
}
package de.tommzn.kafka.lagobserver.persistence;

import de.tommzn.kafka.lagobserver.model.ConsumerLagRecord;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ConsumerLagRepository extends CrudRepository<ConsumerLagRecord, Long> {

    List<ConsumerLagRecord> findByGroupId(String groupId);
    
    ConsumerLagRecord findById(long id);
}
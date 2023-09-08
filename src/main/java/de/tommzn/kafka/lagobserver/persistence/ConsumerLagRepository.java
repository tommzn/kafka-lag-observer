package de.tommzn.kafka.lagobserver.persistence;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

import de.tommzn.kafka.lagobserver.model.ConsumerLagRecord;

public interface ConsumerLagRepository extends CrudRepository<ConsumerLagRecord, Long> {

    List<ConsumerLagRecord> findByGroupId(String groupId);
    
    ConsumerLagRecord findById(long id);
}
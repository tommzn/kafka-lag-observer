package de.tommzn.kafka.lagobserver.controller;

import de.tommzn.kafka.lagobserver.model.ConsumerLag;
import de.tommzn.kafka.lagobserver.model.ConsumerLagRecord;
import de.tommzn.kafka.lagobserver.persistence.ConsumerLagRepository;
import de.tommzn.kafka.lagobserver.service.LagAnalyzerService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerLagController {
    
    @Autowired
    private LagAnalyzerService lagAnalyzerService;
    
    @Autowired
    ConsumerLagRepository repository;

    @GetMapping("/consumerlag")
    public List<ConsumerLag> all() {
        return convertToConsumerLagList(repository.findAll());
    }
    
    @GetMapping("/consumerlag/{groupid}")
    public List<ConsumerLag> byGroupId(@PathVariable String groupid) throws ExecutionException, InterruptedException {
        lagAnalyzerService.analyzeLag(groupid);
        return convertToConsumerLagList(repository.findByGroupId(groupid));
    }

    private List<ConsumerLag> convertToConsumerLagList(Iterable<ConsumerLagRecord> entities) {

        List<ConsumerLag> listOfConsumerLags = new ArrayList<ConsumerLag>();
        entities.forEach( (entity) -> {
            listOfConsumerLags.add(new ConsumerLag(entity.getGroupId(), entity.getTopic(), entity.getPartition(), entity.getLag()));
        });
        return listOfConsumerLags;
    }   
}
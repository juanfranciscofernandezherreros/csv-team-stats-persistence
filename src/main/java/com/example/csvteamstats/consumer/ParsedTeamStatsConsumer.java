package com.example.csvteamstats.consumer;
import com.example.csvteamstats.avro.*;
import com.example.csvteamstats.service.TeamStatsPersistenceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Service
public class ParsedTeamStatsConsumer {
 private final TeamStatsPersistenceService service;
 public ParsedTeamStatsConsumer(TeamStatsPersistenceService service){this.service=service;}
 @KafkaListener(topics="${app.kafka.topics.parsed-team-stats}",groupId="${spring.kafka.consumer.group-id}")
 public void listen(ConsumerRecord<TeamStatsKey,TeamStatsValue> record){
  if(record.value()!=null) service.persist(record.value());
 }
}

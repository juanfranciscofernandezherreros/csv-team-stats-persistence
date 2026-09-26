package com.example.csvteamstats.consumer;

import com.example.csvteamstats.avro.TeamStatsKey;
import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.service.TeamStatsPersistenceService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParsedTeamStatsConsumer {
 private final TeamStatsPersistenceService service;

 public ParsedTeamStatsConsumer(TeamStatsPersistenceService service) {
  this.service = service;
 }

 @KafkaListener(
         topics="${app.kafka.topics.parsed-team-stats}",
         groupId="${spring.kafka.consumer.group-id}"
 )
 public void listen(List<ConsumerRecord<TeamStatsKey, TeamStatsValue>> records) {
  service.persistBatch(records.stream()
          .map(ConsumerRecord::value)
          .filter(java.util.Objects::nonNull)
          .toList());
 }
}

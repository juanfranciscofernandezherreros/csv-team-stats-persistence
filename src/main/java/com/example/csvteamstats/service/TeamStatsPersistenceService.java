package com.example.csvteamstats.service;
import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.mapper.TeamStatsMapper;
import com.example.csvteamstats.repository.TeamStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class TeamStatsPersistenceService {
 private final TeamStatsRepository repository; private final TeamStatsMapper mapper;
 public TeamStatsPersistenceService(TeamStatsRepository repository,TeamStatsMapper mapper){this.repository=repository;this.mapper=mapper;}
 @Transactional
 public TeamStats persist(TeamStatsValue value){
  if(value==null) throw new IllegalArgumentException("TeamStatsValue no puede ser null");
  TeamStats target=repository.findByMatchIdAndPeriodAndCategoryAndMetric(value.getMatchId(),value.getPeriod(),value.getCategory(),value.getMetric()).orElseGet(TeamStats::new);
  return repository.saveAndFlush(mapper.toEntity(value,target));
 }
}

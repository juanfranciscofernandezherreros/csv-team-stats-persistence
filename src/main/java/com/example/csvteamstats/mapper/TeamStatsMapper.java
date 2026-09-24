package com.example.csvteamstats.mapper;
import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import org.springframework.stereotype.Component;
@Component
public class TeamStatsMapper {
 public TeamStats toEntity(TeamStatsValue v,TeamStats target){
  target.setSourceEventId(v.getSourceEventId());
  target.setMatchId(v.getMatchId());target.setPeriod(v.getPeriod());target.setCategory(v.getCategory());target.setMetric(v.getMetric());
  target.setHomeTeam(v.getHomeTeam());target.setHomeValue(v.getHomeValue());target.setAwayTeam(v.getAwayTeam());target.setAwayValue(v.getAwayValue());target.setSourceUrl(v.getSourceUrl());
  return target;
 }
}

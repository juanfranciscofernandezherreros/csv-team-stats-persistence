package com.example.csvteamstats.service;
import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.mapper.TeamStatsMapper;
import com.example.csvteamstats.repository.TeamStatsRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class TeamStatsPersistenceServiceTest {
 @Test void upsertsByBusinessKey(){
  TeamStatsRepository repository=mock(TeamStatsRepository.class);
  TeamStats existing=new TeamStats();
  when(repository.findByMatchIdAndPeriodAndCategoryAndMetric("m1","Overall","Scoring","FGA")).thenReturn(Optional.of(existing));
  when(repository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
  TeamStatsValue v=TeamStatsValue.newBuilder().setSourceEventId("e2").setMatchId("m1").setPeriod("Overall").setCategory("Scoring").setMetric("FGA").setHomeTeam("A").setHomeValue("60").setAwayTeam("B").setAwayValue("55").setSourceUrl("https://example").build();
  TeamStats saved=new TeamStatsPersistenceService(repository,new TeamStatsMapper()).persist(v);
  assertSame(existing,saved);
  assertEquals("60",saved.getHomeValue());
  assertEquals("e2",saved.getSourceEventId());
  verify(repository).saveAndFlush(existing);
 }
}

package com.example.csvteamstats;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.repository.TeamStatsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@DataJpaTest
class PostgreSqlPersistenceIT {
 @Container static final PostgreSQLContainer<?> POSTGRES=new PostgreSQLContainer<>("postgres:16-alpine");
 @DynamicPropertySource static void properties(DynamicPropertyRegistry registry){
  registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);
  registry.add("spring.datasource.username",POSTGRES::getUsername);
  registry.add("spring.datasource.password",POSTGRES::getPassword);
 }
 @Autowired TeamStatsRepository repository;
 @Test void persistsAndQueriesTeamStat(){
  TeamStats s=new TeamStats();s.setMatchId("0fAJZWz1");s.setPeriod("1st Quarter");s.setCategory("Scoring");s.setMetric("Field Goal Attempts");s.setHomeTeam("Bamberg");s.setHomeValue("16");s.setAwayTeam("Bayern");s.setAwayValue("15");s.setSourceEventId("event-1");
  repository.saveAndFlush(s);
  var found=repository.findByMatchIdAndPeriodAndCategoryAndMetric("0fAJZWz1","1st Quarter","Scoring","Field Goal Attempts");
  assertTrue(found.isPresent());assertEquals("16",found.orElseThrow().getHomeValue());
 }
}

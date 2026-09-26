package com.example.csvteamstats;

import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.repository.TeamStatsRepository;
import com.example.csvteamstats.service.TeamStatsPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.properties.schema.registry.url=mock://team-stats-it"
})
class PostgreSqlPersistenceIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    TeamStatsPersistenceService service;

    @Autowired
    TeamStatsRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void insertAndReimportUpdateSameBusinessKey() {
        service.persist(value("event-1", "16"));
        service.persist(value("event-2", "18"));

        assertEquals(1, repository.count());
        var saved = repository
                .findByMatchIdAndPeriodAndCategoryAndMetric(
                        "0fAJZWz1", "1st Quarter", "Scoring", "Field Goal Attempts")
                .orElseThrow();

        assertEquals("18", saved.getHomeValue());
        assertEquals("event-2", saved.getSourceEventId());
    }

    @Test
    void concurrentRedeliveryKeepsSingleTeamStat() throws Exception {
        TeamStatsValue event = value("event-concurrent", "20");
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() -> persistAfter(start, event));
            Future<?> second = executor.submit(() -> persistAfter(start, event));

            start.countDown();
            first.get();
            second.get();

            assertEquals(1, repository.count());
            var saved = repository
                    .findByMatchIdAndPeriodAndCategoryAndMetric(
                            "0fAJZWz1", "1st Quarter", "Scoring", "Field Goal Attempts")
                    .orElseThrow();

            assertEquals("20", saved.getHomeValue());
            assertEquals("event-concurrent", saved.getSourceEventId());
        } finally {
            executor.shutdownNow();
        }
    }

    private void persistAfter(CountDownLatch start, TeamStatsValue event) {
        try {
            start.await();
            service.persist(event);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interrupted);
        }
    }

    private TeamStatsValue value(String sourceEventId, String homeValue) {
        return TeamStatsValue.newBuilder()
                .setSourceEventId(sourceEventId)
                .setMatchId("0fAJZWz1")
                .setPeriod("1st Quarter")
                .setCategory("Scoring")
                .setMetric("Field Goal Attempts")
                .setHomeTeam("Bamberg")
                .setHomeValue(homeValue)
                .setAwayTeam("Bayern")
                .setAwayValue("15")
                .setSourceUrl("https://example")
                .build();
    }
}

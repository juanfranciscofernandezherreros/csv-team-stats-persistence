package com.example.csvteamstats.service;

import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.mapper.TeamStatsMapper;
import com.example.csvteamstats.repository.TeamStatsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamStatsPersistenceServiceTest {

    @Test
    void upsertsByBusinessKey() {
        TeamStatsRepository repository = mock(TeamStatsRepository.class);
        TeamStats existing = new TeamStats();
        when(repository.findByMatchIdAndPeriodAndCategoryAndMetric("m1", "Overall", "Scoring", "FGA"))
                .thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        TeamStatsValue value = value();

        TeamStats saved = new TeamStatsPersistenceService(repository, new TeamStatsMapper()).persist(value);

        assertSame(existing, saved);
        assertEquals("60", saved.getHomeValue());
        assertEquals("e2", saved.getSourceEventId());
        verify(repository).saveAndFlush(existing);
    }

    @Test
    void translatesConcurrentUniqueConflictToRetryableException() {
        TeamStatsRepository repository = mock(TeamStatsRepository.class);
        when(repository.findByMatchIdAndPeriodAndCategoryAndMetric("m1", "Overall", "Scoring", "FGA"))
                .thenReturn(Optional.empty());

        SQLException sqlException = new SQLException("duplicate key", "23505");
        DataIntegrityViolationException conflict =
                new DataIntegrityViolationException("concurrent upsert", sqlException);
        when(repository.saveAndFlush(any())).thenThrow(conflict);

        TeamStatsPersistenceService service =
                new TeamStatsPersistenceService(repository, new TeamStatsMapper());

        assertThrows(ConcurrentUpsertConflictException.class, () -> service.persist(value()));
    }

    @Test
    void keepsOtherIntegrityViolationsPermanent() {
        TeamStatsRepository repository = mock(TeamStatsRepository.class);
        when(repository.findByMatchIdAndPeriodAndCategoryAndMetric("m1", "Overall", "Scoring", "FGA"))
                .thenReturn(Optional.empty());

        DataIntegrityViolationException violation =
                new DataIntegrityViolationException("not null violation", new SQLException("bad row", "23502"));
        when(repository.saveAndFlush(any())).thenThrow(violation);

        TeamStatsPersistenceService service =
                new TeamStatsPersistenceService(repository, new TeamStatsMapper());

        assertThrows(DataIntegrityViolationException.class, () -> service.persist(value()));
    }

    private TeamStatsValue value() {
        return TeamStatsValue.newBuilder()
                .setSourceEventId("e2")
                .setMatchId("m1")
                .setPeriod("Overall")
                .setCategory("Scoring")
                .setMetric("FGA")
                .setHomeTeam("A")
                .setHomeValue("60")
                .setAwayTeam("B")
                .setAwayValue("55")
                .setSourceUrl("https://example")
                .build();
    }
}

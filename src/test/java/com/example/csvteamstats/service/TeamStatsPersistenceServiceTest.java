package com.example.csvteamstats.service;

import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.mapper.TeamStatsMapper;
import com.example.csvteamstats.repository.TeamStatsUpsertRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamStatsPersistenceServiceTest {

    @Test
    void upsertsByBusinessKeyAtomically() {
        TeamStatsUpsertRepository repository = mock(TeamStatsUpsertRepository.class);
        TeamStatsMapper mapper = mock(TeamStatsMapper.class);
        TeamStatsValue value = mock(TeamStatsValue.class);
        TeamStats entity = new TeamStats();
        entity.setHomeValue("60");

        when(mapper.toEntity(eq(value), any(TeamStats.class))).thenReturn(entity);

        TeamStats saved = new TeamStatsPersistenceService(repository, mapper).persist(value);

        assertSame(entity, saved);
        assertEquals("60", saved.getHomeValue());
        verify(repository).upsert(entity);
    }

    @Test
    void rejectsNullValueBeforePersistence() {
        TeamStatsUpsertRepository repository = mock(TeamStatsUpsertRepository.class);
        TeamStatsMapper mapper = mock(TeamStatsMapper.class);
        TeamStatsPersistenceService service = new TeamStatsPersistenceService(repository, mapper);

        assertThrows(IllegalArgumentException.class, () -> service.persist(null));
    }
}

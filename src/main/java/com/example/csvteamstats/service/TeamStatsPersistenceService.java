package com.example.csvteamstats.service;

import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.mapper.TeamStatsMapper;
import com.example.csvteamstats.repository.TeamStatsUpsertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamStatsPersistenceService {

    private final TeamStatsUpsertRepository repository;
    private final TeamStatsMapper mapper;

    public TeamStatsPersistenceService(TeamStatsUpsertRepository repository, TeamStatsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public TeamStats persist(TeamStatsValue value) {
        if (value == null) {
            throw new IllegalArgumentException("TeamStatsValue no puede ser null");
        }

        TeamStats entity = mapper.toEntity(value, new TeamStats());
        repository.upsert(entity);
        return entity;
    }

    @Transactional
    public void persistBatch(List<TeamStatsValue> values) {
        if (values.isEmpty()) {
            return;
        }
        List<TeamStats> stats = values.stream()
                .map(value -> mapper.toEntity(value, new TeamStats()))
                .toList();
        repository.upsertBatch(stats);
    }
}

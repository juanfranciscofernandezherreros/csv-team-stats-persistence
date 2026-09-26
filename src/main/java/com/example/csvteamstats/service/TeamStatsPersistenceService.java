package com.example.csvteamstats.service;

import com.example.csvteamstats.avro.TeamStatsValue;
import com.example.csvteamstats.entity.TeamStats;
import com.example.csvteamstats.mapper.TeamStatsMapper;
import com.example.csvteamstats.repository.TeamStatsRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
public class TeamStatsPersistenceService {
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private final TeamStatsRepository repository;
    private final TeamStatsMapper mapper;

    public TeamStatsPersistenceService(TeamStatsRepository repository, TeamStatsMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public TeamStats persist(TeamStatsValue value) {
        if (value == null) {
            throw new IllegalArgumentException("TeamStatsValue no puede ser null");
        }

        TeamStats target = repository
                .findByMatchIdAndPeriodAndCategoryAndMetric(
                        value.getMatchId(),
                        value.getPeriod(),
                        value.getCategory(),
                        value.getMetric())
                .orElseGet(TeamStats::new);

        try {
            return repository.saveAndFlush(mapper.toEntity(value, target));
        } catch (DataIntegrityViolationException exception) {
            if (isUniqueConstraintConflict(exception)) {
                throw new ConcurrentUpsertConflictException(
                        "Concurrent TEAM_STATS upsert conflict",
                        exception);
            }
            throw exception;
        }
    }

    private boolean isUniqueConstraintConflict(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

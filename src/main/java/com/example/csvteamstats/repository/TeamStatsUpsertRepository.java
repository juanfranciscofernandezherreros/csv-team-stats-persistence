package com.example.csvteamstats.repository;

import com.example.csvteamstats.entity.TeamStats;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TeamStatsUpsertRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO team_stats (
                match_id, period, category, metric,
                home_team, home_value, away_team, away_value,
                source_url, source_event_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (match_id, period, category, metric) DO UPDATE SET
                home_team = EXCLUDED.home_team,
                home_value = EXCLUDED.home_value,
                away_team = EXCLUDED.away_team,
                away_value = EXCLUDED.away_value,
                source_url = EXCLUDED.source_url,
                source_event_id = EXCLUDED.source_event_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public TeamStatsUpsertRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(TeamStats stats) {
        jdbcTemplate.update(
                UPSERT_SQL,
                stats.getMatchId(),
                stats.getPeriod(),
                stats.getCategory(),
                stats.getMetric(),
                stats.getHomeTeam(),
                stats.getHomeValue(),
                stats.getAwayTeam(),
                stats.getAwayValue(),
                stats.getSourceUrl(),
                stats.getSourceEventId()
        );
    }
}

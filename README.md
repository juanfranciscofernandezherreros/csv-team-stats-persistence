Current version: **1.0.0**

# csv-team-stats-persistence

Microservicio de persistencia para estadísticas de equipo y periodo.

```text
team-stats.parsed -> ParsedTeamStatsConsumer -> upsert JPA -> PostgreSQL team_stats
```

Consume exactamente `TeamStatsKey / TeamStatsValue` publicados por `csv-team-stats-parser`.

La unicidad es `(match_id, period, category, metric)`, por lo que una reentrega actualiza los valores y conserva el último `source_event_id`. Los valores se guardan como texto para soportar enteros, decimales y porcentajes sin pérdida.

Variables: `DB_URL`, `DB_USER`, `DB_PASS`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_PARSED_TEAM_STATS_TOPIC`.

Tests: `mvn -B test`. Integración PostgreSQL: `mvn -B verify -Pintegration`.

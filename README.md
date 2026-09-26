![version](https://img.shields.io/badge/version-1.1.2-blue)
# csv-team-stats-persistence

Microservicio de persistencia para estadísticas de equipo y periodo.

```text
team-stats.parsed -> ParsedTeamStatsConsumer -> upsert JPA -> PostgreSQL team_stats
```

Consume exactamente `TeamStatsKey / TeamStatsValue` publicados por `csv-team-stats-parser`.

## Contratos Avro compartidos

`TeamStatsKey` y `TeamStatsValue` se consumen desde:

```text
com.fernandez.basketball:basketball-event-contracts:1.0.2
```

Este repositorio ya no mantiene copias locales de esos schemas ni genera clases Avro durante su propia build. Fuera de GitHub Actions, Maven necesita credenciales con `read:packages` para resolver el artefacto desde GitHub Packages.

La unicidad es `(match_id, period, category, metric)`, por lo que una reentrega actualiza los valores y conserva el último `source_event_id`. Los valores se guardan como texto para soportar enteros, decimales y porcentajes sin pérdida.

Variables: `DB_URL`, `DB_USER`, `DB_PASS`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_PARSED_TEAM_STATS_TOPIC`.

Tests: `mvn -B test`. Integración PostgreSQL: `mvn -B verify -Pintegration`.


## Estrategia de errores Kafka

KAN-113 aplica la política de KAN-18 al consumo de `team-stats.parsed`.

- errores de datos o integridad: non-retryable;
- fallos transitorios de PostgreSQL: retryable;
- mensajes agotados: `team-stats.parsed.DLT`;
- `KAFKA_RETRY_MAX_ATTEMPTS`: intentos totales, default `3`;
- `KAFKA_RETRY_BACKOFF_MS`: backoff fijo, default `1000`;
- `KAFKA_TEAM_STATS_PERSISTENCE_DLT_TOPIC`: topic DLT configurable.

Spring Kafka publica el registro original en DLT con headers de excepción y contexto. Los errores de deserialización Avro se capturan mediante `ErrorHandlingDeserializer`, la DLT admite Avro y bytes crudos, y un fallo al publicar en la DLT se propaga para evitar pérdida silenciosa. Los conflictos únicos concurrentes (`SQLState 23505`) se traducen explícitamente a una excepción transitoria retryable antes de llegar al handler; las demás violaciones de integridad permanecen non-retryable.

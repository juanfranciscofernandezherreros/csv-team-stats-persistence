![version](https://img.shields.io/badge/version-1.3.0-blue)
# csv-team-stats-persistence

Microservicio de persistencia para estadísticas de equipo y periodo.

```text
team-stats.parsed -> ParsedTeamStatsConsumer -> PostgreSQL team_stats
```

Consume exactamente `TeamStatsKey / TeamStatsValue` publicados por `csv-team-stats-parser`.

## Política de idempotencia

KAN-66 aplica la estrategia común de KAN-19 a TEAM-STATS. La clave natural es:

```text
(match_id, period, category, metric)
```

PostgreSQL la protege mediante `uk_team_stats_match_period_category_metric`. La escritura usa una única operación atómica:

```sql
INSERT ...
ON CONFLICT (match_id, period, category, metric)
DO UPDATE SET ...;
```

Se elimina el patrón `findBy... + saveAndFlush()`, por lo que no existe ventana read-then-write ni es necesario convertir conflictos únicos `23505` en retries como mecanismo normal de concurrencia. Un redelivery mantiene una única fila, una reimportación actualiza los valores y `source_event_id` conserva el evento que produjo el estado vigente.

## Contratos Avro compartidos

`TeamStatsKey` y `TeamStatsValue` se consumen desde:

```text
com.fernandez.basketball:basketball-event-contracts:1.0.2
```

Este repositorio ya no mantiene copias locales de esos schemas ni genera clases Avro durante su propia build. Fuera de GitHub Actions, Maven necesita credenciales con `read:packages` para resolver el artefacto desde GitHub Packages.

Los valores se guardan como texto para soportar enteros, decimales y porcentajes sin pérdida.

Variables: `DB_URL`, `DB_USER`, `DB_PASS`, `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_PARSED_TEAM_STATS_TOPIC`.

## Persistencia batch

KAN-22 cambia TEAM-STATS a consumo Kafka batch. Cada poll de hasta `KAFKA_MAX_POLL_RECORDS=500` registros se mapea y persiste en una sola llamada `JdbcTemplate.batchUpdate(...)`. PostgreSQL usa `reWriteBatchedInserts=true` para reducir round-trips.

Se conserva el `ON CONFLICT (match_id, period, category, metric) DO UPDATE` de KAN-66, por lo que el redelivery del poll mantiene la idempotencia. Un fallo de escritura hace fallar la transacción completa y vuelve a la estrategia Kafka retry/DLT existente.

La integración con Testcontainers mide 1.000 métricas con upsert secuencial frente a JDBC batch y publica ambos throughputs y su ratio en CI.

Tests: `mvn -B test`. Integración PostgreSQL: `mvn -B verify -Pintegration`.

## Estrategia de errores Kafka

KAN-113 aplica la política de KAN-18 al consumo de `team-stats.parsed`.

- errores de datos o integridad: non-retryable;
- fallos transitorios de PostgreSQL: retryable;
- mensajes agotados: `team-stats.parsed.DLT`;
- `KAFKA_RETRY_MAX_ATTEMPTS`: intentos totales, default `3`;
- `KAFKA_RETRY_BACKOFF_MS`: backoff fijo, default `1000`;
- `KAFKA_TEAM_STATS_PERSISTENCE_DLT_TOPIC`: topic DLT configurable.

Spring Kafka publica el registro original en DLT con headers de excepción y contexto. Los errores de deserialización Avro se capturan mediante `ErrorHandlingDeserializer`, la DLT admite Avro y bytes crudos, y un fallo al publicar en la DLT se propaga para evitar pérdida silenciosa.

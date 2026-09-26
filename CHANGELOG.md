# Changelog

## 1.3.0 - 2026-09-26

- [minor] KAN-22 cambia TEAM-STATS persistence a consumo Kafka batch con `KAFKA_MAX_POLL_RECORDS=500` por defecto.
- [minor] Persiste cada poll mediante `JdbcTemplate.batchUpdate(...)` manteniendo el upsert atómico de KAN-66.
- [minor] Activa `reWriteBatchedInserts=true` para reducir round-trips PostgreSQL.
- [minor] Añade tests batch y benchmark Testcontainers de 1.000 métricas secuencial vs JDBC batch.

## 1.2.0 - 2026-09-26

- [minor] KAN-66 sustituye `findBy... + saveAndFlush()` por `ON CONFLICT (match_id, period, category, metric) DO UPDATE`.
- [minor] Elimina la carrera read-then-write y el flush explícito por mensaje.
- [minor] Conserva `source_event_id` de la reimportación que produjo el estado vigente.
- [minor] Añade tests de integración de insert/update y concurrencia.

## 1.1.2 - 2026-09-26

- [patch] KAN-113 cablea la clasificación de conflictos únicos concurrentes en el flujo real de persistencia.
- [patch] Convierte únicamente PostgreSQL `23505` en una excepción transitoria retryable.
- [patch] Mantiene el resto de `DataIntegrityViolationException` como non-retryable en el `DefaultErrorHandler`.
- [patch] Añade tests de servicio y clasificación para ambos caminos.


## 1.1.1 - 2026-09-26

- [patch] KAN-113 enruta errores de deserialización Avro a DLT mediante `ErrorHandlingDeserializer`.
- [patch] Permite publicar en DLT objetos Avro o bytes crudos y deja que Kafka seleccione una partición válida.
- [patch] Hace fallar la recuperación si la publicación en DLT falla, evitando pérdida silenciosa.
- [patch] Trata conflictos únicos concurrentes PostgreSQL (`23505`) como retryable y mantiene otras violaciones de integridad como non-retryable.
- [patch] Añade cobertura para deserialización → DLT, fallo de publicación DLT y conflicto concurrente.


## 1.1.0 - 2026-09-26

- [minor] KAN-113 aplica la estrategia común de errores Kafka de KAN-18.
- [minor] Clasifica errores de datos/integridad como non-retryable y fallos transitorios de PostgreSQL como retryable.
- [minor] Configura retries/backoff y DLT `team-stats.parsed.DLT`.
- [minor] Añade tests de error permanente y transitorio.


## 1.0.5 - 2026-09-25

- [patch] KAN-87 sustituye los schemas locales TeamStats por `basketball-event-contracts:1.0.2`.
- [patch] Elimina la generación Avro local y configura Maven/CI con lectura autenticada de GitHub Packages.
- [patch] Mantiene los namespaces, campos y persistencia existentes sin cambios funcionales.

## 1.0.4 - 2026-09-25

- [patch] Refuerza AGENTS.md: lectura obligatoria por tarea, flujo autónomo y prohibición absoluta de escrituras directas en main.

## 1.0.3

- [patch] Estandariza la automatización del repositorio con el flujo autónomo de csv-results-parser.

## 1.0.2 - 2026-09-25

- [patch] Añade eliminación automática de la rama origen después de mergear una Pull Request en `main`.

## 1.0.1 - 2026-09-25

- [patch] Exige confirmar rama y nivel SemVer antes de cualquier cambio.
- [patch] Alinea Maven CI-friendly con revision, sha1 y changelist.

## 1.0.0 - 2026-09-24
- Consume `team-stats.parsed` en Avro.
- Persiste estadísticas Overall y por cuarto en PostgreSQL `team_stats`.
- Hace upsert por `(match_id, period, category, metric)`.
- Añade índices por partido y por partido/periodo para facilitar la futura API agregadora.

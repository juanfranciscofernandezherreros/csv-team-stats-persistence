# Changelog

## 1.0.1 - 2026-09-25

- [patch] Exige confirmar rama y nivel SemVer antes de cualquier cambio.
- [patch] Alinea Maven CI-friendly con revision, sha1 y changelist.

## 1.0.0 - 2026-09-24
- Consume `team-stats.parsed` en Avro.
- Persiste estadísticas Overall y por cuarto en PostgreSQL `team_stats`.
- Hace upsert por `(match_id, period, category, metric)`.
- Añade índices por partido y por partido/periodo para facilitar la futura API agregadora.

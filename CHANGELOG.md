# Changelog

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

-- ================================================================
-- Migración: video de interpretación en lengua de señas sincronizado (RF-040 / HU-040)
-- Para bases de datos YA creadas (el esquema completo ya incluye la columna).
-- Idempotente: se puede ejecutar más de una vez.
--
-- Regla de negocio:
--   * El instructor asocia un video de intérprete (enlace) a un video o una lectura y ajusta el
--     desfase respecto al original; el estudiante lo ve al lado y sincronizado (< 300 ms).
-- ================================================================
ALTER TABLE recursos_accesibles
    ADD COLUMN IF NOT EXISTS desfase_ms INT NOT NULL DEFAULT 0;

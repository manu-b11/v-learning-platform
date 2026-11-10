-- ================================================================
-- Migración: preferencias de accesibilidad del Sprint 9
-- Para bases de datos YA creadas (el esquema completo ya incluye las columnas).
-- Idempotente: se puede ejecutar más de una vez.
--
-- Regla de negocio:
--   * RF-035 / HU-035: subrayar todos los enlaces. La fuente OpenDyslexic se guarda en la
--     columna tipografia existente (no requiere columna nueva).
-- ================================================================
ALTER TABLE configuraciones_accesibilidad
    ADD COLUMN IF NOT EXISTS subrayar_enlaces BOOLEAN NOT NULL DEFAULT FALSE;

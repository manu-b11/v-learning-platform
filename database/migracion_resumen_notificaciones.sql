-- ================================================================
-- Migración: resumen diario de notificaciones y pausa de la gamificación (RF-047 / HU-047)
-- Para bases de datos YA creadas (el esquema completo ya incluye estas columnas).
-- Idempotente: se puede ejecutar más de una vez.
--
-- Regla de negocio:
--   * Con el resumen diario activo, las notificaciones no urgentes se guardan y se muestran
--     juntas a la hora elegida (un proceso cada 5 minutos envía el resumen, ±5 min).
--   * Urgentes (obligatorias o fechas límite en menos de 24 horas): se envían de inmediato.
--   * Se activa por defecto al elegir el perfil cognitivo (18:00), si la persona no lo eligió antes.
--   * La pausa de la gamificación oculta avisos de puntos, insignias y ranking sin dejar de
--     registrar los puntos.
-- ================================================================
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS resumen_diario BOOLEAN NULL;
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS hora_resumen TIME(0) NULL;

ALTER TABLE notificaciones
    ADD COLUMN IF NOT EXISTS en_resumen BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE notificaciones
    ADD COLUMN IF NOT EXISTS urgente BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE perfiles_gamificacion
    ADD COLUMN IF NOT EXISTS avisos_pausados BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_notificaciones_resumen ON notificaciones (en_resumen, pospuesta_hasta);

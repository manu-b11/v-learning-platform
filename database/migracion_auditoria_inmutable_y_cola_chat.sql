-- ================================================================
-- Migración para bases de datos YA creadas (el esquema completo ya incluye estos cambios).
-- Idempotente: se puede ejecutar más de una vez. Ejecutar como dueño de las tablas (vlearning_admin).
--
-- 1) HU-015 / RF-014: registros_auditoria pasa a ser de solo inserción, aplicado por el motor.
-- 2) HU-012 / RF-010: cola de reenvío del chat configurable por el instructor en cada curso.
-- ================================================================

CREATE OR REPLACE FUNCTION fn_auditoria_inmutable() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'UPDATE'
       AND pg_trigger_depth() > 1
       AND OLD.usuario_id IS NOT NULL AND NEW.usuario_id IS NULL
       AND NEW.id = OLD.id
       AND NEW.accion = OLD.accion
       AND NEW.fecha = OLD.fecha
       AND NEW.ip_origen IS NOT DISTINCT FROM OLD.ip_origen
       AND NEW.resultado = OLD.resultado
       AND NEW.recurso IS NOT DISTINCT FROM OLD.recurso THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'registros_auditoria es de solo inserción: % no permitido', TG_OP
        USING ERRCODE = 'insufficient_privilege';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_registros_auditoria_inmutable ON registros_auditoria;
CREATE TRIGGER trg_registros_auditoria_inmutable
    BEFORE UPDATE OR DELETE ON registros_auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria_inmutable();

DROP TRIGGER IF EXISTS trg_registros_auditoria_sin_truncate ON registros_auditoria;
CREATE TRIGGER trg_registros_auditoria_sin_truncate
    BEFORE TRUNCATE ON registros_auditoria
    FOR EACH STATEMENT EXECUTE FUNCTION fn_auditoria_inmutable();

ALTER TABLE cursos
    ADD COLUMN IF NOT EXISTS cola_reenvio_chat BOOLEAN NOT NULL DEFAULT TRUE;

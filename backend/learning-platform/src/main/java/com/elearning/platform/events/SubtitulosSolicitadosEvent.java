package com.elearning.platform.events;

/** RF-037: se pidió un borrador de subtítulos; se genera en segundo plano tras confirmar la transacción. */
public record SubtitulosSolicitadosEvent(Long contenidoId, Long instructorId) {}

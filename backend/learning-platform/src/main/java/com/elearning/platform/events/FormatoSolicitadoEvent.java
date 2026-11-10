package com.elearning.platform.events;

import com.elearning.platform.entity.FormatoAlternativo;

/** RF-032: se pidió generar un formato alternativo; se genera en segundo plano tras la transacción. */
public record FormatoSolicitadoEvent(Long formatoId, FormatoAlternativo.Tipo tipo, Long usuarioId) {}

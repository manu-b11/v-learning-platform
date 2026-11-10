package com.elearning.platform.controller;

import com.elearning.platform.dto.DashboardDtos.DashboardRespuesta;
import com.elearning.platform.dto.GamificacionDtos.*;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.DashboardService;
import com.elearning.platform.services.GamificacionService;
import com.elearning.platform.services.RankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** RF-006 dashboard y RF-009 logros, privacidad y ranking (estudiante). */
@RestController
@PreAuthorize("hasRole('ESTUDIANTE')")
@RequiredArgsConstructor
public class LogrosController {

    private final DashboardService dashboard;
    private final GamificacionService gamificacion;
    private final RankingService ranking;

    @GetMapping("/progreso")
    public DashboardRespuesta progreso(@AuthenticationPrincipal UsuarioPrincipal u) {
        return dashboard.dashboard(u.id());
    }

    @GetMapping("/logros")
    public LogrosRespuesta logros(@AuthenticationPrincipal UsuarioPrincipal u) {
        return gamificacion.logros(u.id());
    }

    /** Puntos ganados por día en los últimos N días (por defecto 7); alimenta la gráfica de actividad del inicio. */
    @GetMapping("/logros/actividad")
    public ActividadRespuesta actividad(@AuthenticationPrincipal UsuarioPrincipal u,
                                        @RequestParam(defaultValue = "7") int dias) {
        return gamificacion.actividad(u.id(), dias);
    }

    @PutMapping("/logros/privacidad")
    public LogrosRespuesta privacidad(@AuthenticationPrincipal UsuarioPrincipal u, @Valid @RequestBody PrivacidadRequest req) {
        return gamificacion.cambiarPrivacidad(u.id(), req.mostrarEnRanking());
    }

    /** RF-047: pausar los avisos de gamificación sin perder los puntos. */
    @io.swagger.v3.oas.annotations.Operation(summary = "Pausar o reanudar los avisos de gamificación (RF-047)",
            description = "Con la pausa activa no se avisan puntos, niveles ni insignias y se oculta el ranking; los puntos se siguen registrando.")
    @PutMapping("/logros/pausa")
    public LogrosRespuesta pausa(@AuthenticationPrincipal UsuarioPrincipal u, @Valid @RequestBody PausaGamificacionRequest req) {
        return gamificacion.pausarAvisos(u.id(), req.pausada());
    }

    @GetMapping("/cursos/{id}/ranking")
    public RankingRespuesta ranking(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return ranking.obtener(id, u.id());
    }
}

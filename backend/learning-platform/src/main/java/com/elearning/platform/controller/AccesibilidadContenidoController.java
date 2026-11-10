package com.elearning.platform.controller;

import com.elearning.platform.dto.VerificacionDtos.InformeAccesibilidad;
import com.elearning.platform.dto.VerificacionDtos.VerificacionBorradorRequest;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.AccesibilidadContenidoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** RF-049: verificador de accesibilidad del contenido para el instructor dueño del curso. */
@RestController
@RequestMapping("/contenidos/{id}/accesibilidad")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
public class AccesibilidadContenidoController {

    private final AccesibilidadContenidoService servicio;

    @Operation(summary = "Verificar la accesibilidad de un contenido (RF-049)",
            description = "Ejecuta la verificación completa con lo guardado y actualiza el informe del contenido. "
                    + "Cada hallazgo trae su criterio WCAG, su severidad (BLOQUEANTE o ADVERTENCIA) y cómo corregirlo.")
    @GetMapping
    public InformeAccesibilidad verificar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return servicio.verificar(id, u.id());
    }

    @Operation(summary = "Verificar en vivo lo que se está escribiendo (RF-049)",
            description = "Analiza el borrador del editor sin guardarlo. Los recursos accesibles se leen de la base.")
    @PostMapping("/verificar")
    public InformeAccesibilidad verificarBorrador(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id,
                                                  @Valid @RequestBody VerificacionBorradorRequest req) {
        return servicio.verificarBorrador(id, u.id(), req);
    }
}

package com.elearning.platform.controller;

import com.elearning.platform.dto.AcomodacionDtos.AcomodacionRequest;
import com.elearning.platform.dto.AcomodacionDtos.AcomodacionVista;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.AcomodacionService;
import com.elearning.platform.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RF-041: acomodaciones de evaluación por estudiante. Solo instructor del curso y administrador. */
@RestController
@RequestMapping("/cursos/{cursoId}/acomodaciones")
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMINISTRADOR')")
@RequiredArgsConstructor
public class AcomodacionController {

    private final AcomodacionService servicio;

    @Operation(summary = "Listar las acomodaciones activas del curso (RF-041)")
    @GetMapping
    public List<AcomodacionVista> listar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long cursoId) {
        return servicio.listar(cursoId, u);
    }

    @Operation(summary = "Asignar una acomodación a un estudiante inscrito (RF-041)",
            description = "Reemplaza la acomodación activa del estudiante. Multiplicador 1,5x o 2x, tiempo ilimitado, "
                    + "intentos extra, fecha límite especial, minutos de ampliación (RF-042) y vigencia. Queda en auditoría.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcomodacionVista asignar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long cursoId,
                                    @Valid @RequestBody AcomodacionRequest req, HttpServletRequest http) {
        return servicio.asignar(cursoId, req, u, IpUtil.de(http));
    }

    @Operation(summary = "Retirar una acomodación (RF-041)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long cursoId, @PathVariable Long id,
                       HttpServletRequest http) {
        servicio.quitar(cursoId, id, u, IpUtil.de(http));
    }
}

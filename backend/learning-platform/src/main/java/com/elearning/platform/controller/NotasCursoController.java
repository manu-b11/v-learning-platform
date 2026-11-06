package com.elearning.platform.controller;

import com.elearning.platform.dto.NotasDtos.NotasCurso;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.NotasCursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** RF-029: notas del curso con promedio general y quiénes aprueban (instructor del curso o administrador). */
@RestController
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMINISTRADOR')")
@RequiredArgsConstructor
public class NotasCursoController {

    private final NotasCursoService notas;

    @GetMapping("/cursos/{id}/notas")
    public NotasCurso notas(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return notas.notas(u, id);
    }
}

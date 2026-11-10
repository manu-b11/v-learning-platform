package com.elearning.platform.controller;

import com.elearning.platform.dto.ContenidoDtos.*;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.ContenidoService;
import com.elearning.platform.services.ProgresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Lección, apoyo cognitivo y registro de avance. */
@RestController
@RequiredArgsConstructor
public class ContenidoController {

    private final ContenidoService contenidos;
    private final ProgresoService progreso;
    private final com.elearning.platform.services.AudioContenidoService audio;

    /** RF-036: lectura en voz alta en MP3 (o ZIP con pistas numeradas si el contenido es largo). */
    @io.swagger.v3.oas.annotations.Operation(summary = "Descargar en MP3 la lectura en voz alta (RF-036)",
            description = "velocidad de 0,5 a 2 (la del reproductor). Si el texto supera 1.500 palabras se entrega un ZIP "
                    + "con pistas numeradas. El archivo lleva el título del contenido. Queda en auditoría.")
    @GetMapping("/contenidos/{id}/audio")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    public org.springframework.http.ResponseEntity<byte[]> audio(@AuthenticationPrincipal UsuarioPrincipal u,
                                                                 @PathVariable Long id,
                                                                 @RequestParam(defaultValue = "1") double velocidad,
                                                                 jakarta.servlet.http.HttpServletRequest http) {
        var a = audio.descargar(u, id, velocidad, com.elearning.platform.util.IpUtil.de(http));
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
                                .filename(a.nombre(), java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(org.springframework.http.MediaType.parseMediaType(a.tipoContenido()))
                .body(a.contenido());
    }

    @GetMapping("/contenidos/{id}")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR','ADMINISTRADOR')")
    public ContenidoVista ver(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return contenidos.ver(u, id);
    }

    @GetMapping("/contenidos/{id}/apoyo-cognitivo")
    @PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR')")
    public ApoyoCognitivoRespuesta apoyo(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return contenidos.apoyoCognitivo(u, id);
    }

    @PostMapping("/contenidos/{id}/progreso")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ProgresoRespuesta registrar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id,
                                       @Valid @RequestBody ProgresoRequest req) {
        return progreso.registrar(u.id(), id, req);
    }
}

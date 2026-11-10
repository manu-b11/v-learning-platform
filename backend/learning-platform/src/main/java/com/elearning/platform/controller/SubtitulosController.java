package com.elearning.platform.controller;

import com.elearning.platform.dto.SubtitulosDtos.BorradorVista;
import com.elearning.platform.dto.SubtitulosDtos.EdicionRequest;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.SubtitulosService;
import com.elearning.platform.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** RF-037: borrador de subtítulos automáticos para revisión y aprobación del instructor. */
@RestController
@RequiredArgsConstructor
public class SubtitulosController {

    private final SubtitulosService servicio;

    @Operation(summary = "Ver el borrador de subtítulos (RF-037)")
    @GetMapping("/contenidos/{id}/subtitulos/borrador")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public BorradorVista ver(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return servicio.ver(id, u.id());
    }

    @Operation(summary = "Generar el borrador automático de subtítulos (RF-037)",
            description = "Responde 202: se genera en segundo plano y se notifica al instructor cuando está listo.")
    @PostMapping("/contenidos/{id}/subtitulos/borrador")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BorradorVista generar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id, HttpServletRequest http) {
        return servicio.solicitar(id, u.id(), IpUtil.de(http));
    }

    @Operation(summary = "Guardar las correcciones del borrador (RF-037)",
            description = "Texto y marcas de tiempo en milisegundos. Queda LISTO y hay que aprobarlo de nuevo.")
    @PutMapping("/contenidos/{id}/subtitulos/borrador")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public BorradorVista editar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id,
                                @Valid @RequestBody EdicionRequest req, HttpServletRequest http) {
        return servicio.editar(id, u.id(), req.lineas(), IpUtil.de(http));
    }

    @Operation(summary = "Aprobar los subtítulos (RF-037)",
            description = "Crea el recurso de subtítulos (video) y la transcripción si falta; habilita la publicación.")
    @PostMapping("/contenidos/{id}/subtitulos/borrador/aprobar")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public BorradorVista aprobar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id, HttpServletRequest http) {
        return servicio.aprobar(id, u.id(), IpUtil.de(http));
    }

    @Operation(summary = "Subir subtítulos .srt o .vtt (RF-037)",
            description = "Alternativa si el reconocimiento de voz falla. Máximo 1 MB. Queda como borrador por aprobar.")
    @PostMapping(value = "/contenidos/{id}/subtitulos/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public BorradorVista subir(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id,
                               @RequestParam("archivo") MultipartFile archivo, HttpServletRequest http) {
        return servicio.subirArchivo(id, u.id(), archivo, IpUtil.de(http));
    }

    /** VTT aprobado: público para que el navegador lo cargue en el &lt;track&gt; (como las portadas). */
    @Operation(summary = "Subtítulos aprobados en VTT (RF-037, RF-038)")
    @GetMapping("/archivos/subtitulos/{id}.vtt")
    public ResponseEntity<String> vtt(@PathVariable Long id) {
        return servicio.vttAprobado(id)
                .map(v -> ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .contentType(MediaType.parseMediaType("text/vtt;charset=UTF-8")).body(v))
                .orElse(ResponseEntity.notFound().build());
    }
}

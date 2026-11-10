package com.elearning.platform.controller;

import com.elearning.platform.dto.FormatoDtos.FormatoVista;
import com.elearning.platform.dto.FormatoDtos.FormatosRespuesta;
import com.elearning.platform.entity.FormatoAlternativo;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.formatos.FormatosService;
import com.elearning.platform.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/** RF-032: descargar contenidos en formatos alternativos (MP3, EPUB, HTML accesible y BRF). */
@RestController
@RequestMapping("/contenidos/{id}/formatos")
@PreAuthorize("hasAnyRole('ESTUDIANTE','INSTRUCTOR','ADMINISTRADOR')")
@RequiredArgsConstructor
public class FormatosController {

    private final FormatosService servicio;

    @Operation(summary = "Formatos alternativos disponibles y su estado (RF-032)",
            description = "Videos y simulaciones solo ofrecen su transcripción (soloTranscripcion=true).")
    @GetMapping
    public FormatosRespuesta listar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id) {
        return servicio.listar(u, id);
    }

    @Operation(summary = "Pedir un formato alternativo (RF-032)",
            description = "tipo: MP3, EPUB, HTML o BRF. Si ya está generado y el contenido no cambió, se reutiliza; si no, se "
                    + "genera en segundo plano (202) y se avisa al estudiante.")
    @PostMapping("/{tipo}")
    public ResponseEntity<FormatoVista> solicitar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id,
                                                  @PathVariable FormatoAlternativo.Tipo tipo, HttpServletRequest http) {
        FormatoVista v = servicio.solicitar(u, id, tipo, IpUtil.de(http));
        return ResponseEntity.status("LISTO".equals(v.estado()) ? HttpStatus.OK : HttpStatus.ACCEPTED).body(v);
    }

    @Operation(summary = "Descargar un formato ya generado (RF-032)", description = "Queda en auditoría (FORMATO_DESCARGADO).")
    @GetMapping("/{tipo}/archivo")
    public ResponseEntity<byte[]> descargar(@AuthenticationPrincipal UsuarioPrincipal u, @PathVariable Long id,
                                            @PathVariable FormatoAlternativo.Tipo tipo, HttpServletRequest http) {
        var a = servicio.descargar(u, id, tipo, IpUtil.de(http));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(a.nombre(), StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(a.tipoContenido()))
                .body(a.contenido());
    }
}

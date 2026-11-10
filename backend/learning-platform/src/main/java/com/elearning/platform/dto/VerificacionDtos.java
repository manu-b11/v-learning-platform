package com.elearning.platform.dto;

import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.SeveridadHallazgo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** RF-049: DTOs del verificador de accesibilidad del contenido. */
public final class VerificacionDtos {

    private VerificacionDtos() {}

    /** Un problema de accesibilidad con su criterio WCAG y cómo corregirlo. */
    public record Hallazgo(String regla, String criterioWcag, SeveridadHallazgo severidad, String mensaje,
                           String comoCorregir) {}

    /** Hallazgo con el contenido al que pertenece (para el informe del curso). */
    public record HallazgoVista(Long contenidoId, String contenido, String regla, String criterioWcag,
                                SeveridadHallazgo severidad, String mensaje, String comoCorregir) {}

    public record InformeAccesibilidad(Long contenidoId, String titulo, int bloqueantes, int advertencias,
                                       List<HallazgoVista> hallazgos, LocalDateTime fechaVerificacion) {}

    /** Verificación en vivo desde el editor, con lo que el instructor está escribiendo (no se guarda). */
    public record VerificacionBorradorRequest(
            @NotNull(message = "Elige el formato.") FormatoContenido formato,
            @Size(max = 500, message = "Máximo 500 caracteres.") String urlRecurso,
            @Size(max = 100_000, message = "El texto es demasiado largo.") String cuerpo) {}

    /** Cuerpo de la respuesta 422 cuando la verificación bloquea la publicación. */
    public record PublicacionBloqueada(String codigo, String mensaje, List<HallazgoVista> hallazgos,
                                       LocalDateTime timestamp, String ruta) {}
}

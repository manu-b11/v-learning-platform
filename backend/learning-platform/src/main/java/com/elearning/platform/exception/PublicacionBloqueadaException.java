package com.elearning.platform.exception;

import com.elearning.platform.dto.VerificacionDtos.HallazgoVista;
import org.springframework.http.HttpStatus;

import java.util.List;

/** RF-049: la verificación de accesibilidad encontró problemas bloqueantes (HTTP 422 con la lista). */
public class PublicacionBloqueadaException extends ApiException {

    private final transient List<HallazgoVista> hallazgos;

    public PublicacionBloqueadaException(String codigo, String mensaje, List<HallazgoVista> hallazgos) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, codigo, mensaje);
        this.hallazgos = List.copyOf(hallazgos);
    }

    public List<HallazgoVista> getHallazgos() {
        return hallazgos;
    }
}

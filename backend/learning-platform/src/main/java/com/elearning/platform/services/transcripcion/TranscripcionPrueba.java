package com.elearning.platform.services.transcripcion;

import com.elearning.platform.util.Vtt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RF-037: implementación de prueba. No escucha el audio: devuelve un borrador con líneas de
 * ejemplo para que el instructor practique el flujo de revisión y aprobación. Activa por defecto.
 */
@Component
@ConditionalOnProperty(name = "vlearning.transcripcion.proveedor", havingValue = "prueba", matchIfMissing = true)
public class TranscripcionPrueba implements ServicioTranscripcion {

    @Override
    public List<Vtt.Linea> transcribir(String urlMedio) {
        if (urlMedio == null || urlMedio.isBlank()) throw new IllegalArgumentException("El contenido no tiene enlace al medio.");
        return List.of(
                new Vtt.Linea(0, 4000, "[Borrador automático de prueba] Escribe aquí lo que se dice al inicio."),
                new Vtt.Linea(4000, 9000, "Revisa cada línea, corrige el texto y ajusta los tiempos."),
                new Vtt.Linea(9000, 14000, "Cuando todo esté bien, aprueba los subtítulos para poder publicar."));
    }

    @Override
    public String nombre() {
        return "prueba";
    }
}

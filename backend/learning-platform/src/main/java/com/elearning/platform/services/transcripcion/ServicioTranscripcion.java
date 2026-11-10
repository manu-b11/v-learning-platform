package com.elearning.platform.services.transcripcion;

import com.elearning.platform.util.Vtt;

import java.util.List;

/**
 * RF-037: reconocimiento de voz para el borrador de subtítulos. La implementación se elige con
 * vlearning.transcripcion.proveedor: «prueba» (por defecto, sin dependencias) o «whisper»
 * (whisper.cpp y ffmpeg locales). El resultado es solo un borrador: nunca se publica sin que el
 * instructor lo revise y lo apruebe.
 */
public interface ServicioTranscripcion {

    /**
     * @param urlMedio enlace del video o del audio (http/https)
     * @return líneas con tiempo; lanza una excepción si el reconocimiento falla
     */
    List<Vtt.Linea> transcribir(String urlMedio);

    String nombre();
}

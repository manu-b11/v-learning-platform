package com.elearning.platform.services.voz;

/**
 * RF-036 (y RF-032, formato MP3): convierte texto en audio MP3 en el servidor. La implementación
 * se elige con la propiedad vlearning.tts.proveedor: «prueba» (por defecto, sin dependencias) o
 * «espeak» (espeak-ng + ffmpeg instalados en el servidor). No usa servicios de pago en la nube.
 */
public interface ServicioTextoAVoz {

    /**
     * @param texto     texto plano en español
     * @param velocidad 0,5 a 2 (1 = normal), la misma escala que la lectura en voz alta del navegador
     * @return bytes de un archivo MP3
     */
    byte[] sintetizar(String texto, double velocidad);

    /** Nombre corto del proveedor, para el registro y la auditoría. */
    String nombre();
}

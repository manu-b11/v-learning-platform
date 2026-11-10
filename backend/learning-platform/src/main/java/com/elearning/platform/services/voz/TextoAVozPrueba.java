package com.elearning.platform.services.voz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RF-036: implementación de prueba de ServicioTextoAVoz. Devuelve un MP3 válido de un segundo de
 * silencio (tramas MPEG-1 capa III vacías), para probar el flujo completo sin motor de voz.
 * Se usa cuando vlearning.tts.proveedor=prueba o no está definida.
 */
@Component
@ConditionalOnProperty(name = "vlearning.tts.proveedor", havingValue = "prueba", matchIfMissing = true)
public class TextoAVozPrueba implements ServicioTextoAVoz {

    /** MPEG-1, capa III, sin CRC, 128 kbit/s, 44,1 kHz, mono: cada trama ocupa 417 bytes y dura 26,1 ms. */
    private static final byte[] CABECERA = {(byte) 0xFF, (byte) 0xFB, (byte) 0x90, (byte) 0xC0};
    static final int BYTES_TRAMA = 417;
    static final int TRAMAS_POR_SEGUNDO = 39;

    @Override
    public byte[] sintetizar(String texto, double velocidad) {
        return silencio(TRAMAS_POR_SEGUNDO);
    }

    @Override
    public String nombre() {
        return "prueba";
    }

    static byte[] silencio(int tramas) {
        byte[] mp3 = new byte[tramas * BYTES_TRAMA];
        for (int i = 0; i < tramas; i++) System.arraycopy(CABECERA, 0, mp3, i * BYTES_TRAMA, CABECERA.length);
        return mp3;
    }
}

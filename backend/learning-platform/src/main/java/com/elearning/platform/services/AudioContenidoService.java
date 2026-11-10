package com.elearning.platform.services;

import com.elearning.platform.dto.InformeDtos.ArchivoGenerado;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.voz.ServicioTextoAVoz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * RF-036: descarga en MP3 de la lectura en voz alta de un contenido, con la velocidad elegida por
 * el estudiante. Los contenidos largos se dividen en pistas numeradas (se entregan en un ZIP).
 */
@Service
@RequiredArgsConstructor
public class AudioContenidoService {

    /** RF-036: palabras máximas por pista (unos 10 minutos de audio a velocidad normal). */
    static final int PALABRAS_POR_PISTA = 1500;
    static final double VELOCIDAD_MINIMA = 0.5;
    static final double VELOCIDAD_MAXIMA = 2.0;

    private final ContenidoService contenidos;
    private final ServicioTextoAVoz voz;
    private final AuditoriaService auditoria;

    @Transactional(readOnly = true)
    public ArchivoGenerado descargar(UsuarioPrincipal u, Long contenidoId, double velocidad, String ip) {
        if (velocidad < VELOCIDAD_MINIMA || velocidad > VELOCIDAD_MAXIMA) {
            throw ApiException.solicitudInvalida("VELOCIDAD_INVALIDA", "La velocidad debe estar entre 0,5 y 2.");
        }
        ContenidoService.TextoLeible t = contenidos.textoLeible(u, contenidoId);
        if (t.texto().isBlank()) {
            throw ApiException.conflicto("SIN_TEXTO", "Este contenido no tiene texto para convertir en audio.");
        }
        List<String> pistas = dividir(t.texto(), PALABRAS_POR_PISTA);
        String base = nombreArchivo(t.titulo());
        ArchivoGenerado archivo;
        if (pistas.size() == 1) {
            archivo = new ArchivoGenerado(base + ".mp3", "audio/mpeg", voz.sintetizar(pistas.get(0), velocidad));
        } else {
            archivo = new ArchivoGenerado(base + ".zip", "application/zip", zip(base, pistas, velocidad));
        }
        auditoria.registrar(u.id(), "AUDIO_DESCARGADO", "contenidos/" + contenidoId + " pistas=" + pistas.size()
                + " velocidad=" + velocidad, ResultadoAuditoria.PERMITIDO, ip);
        return archivo;
    }

    /** RF-036: divide el texto en pistas de hasta maxPalabras, sin cortar frases. */
    static List<String> dividir(String texto, int maxPalabras) {
        List<String> pistas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        int palabras = 0;
        for (String frase : texto.trim().split("(?<=[.!?…])\\s+|\\R+")) {
            String f = frase.trim();
            if (f.isEmpty()) continue;
            int n = f.split("\\s+").length;
            if (palabras > 0 && palabras + n > maxPalabras) {
                pistas.add(actual.toString().trim());
                actual.setLength(0);
                palabras = 0;
            }
            actual.append(f).append(' ');
            palabras += n;
        }
        if (!actual.isEmpty()) pistas.add(actual.toString().trim());
        return pistas;
    }

    /** Título del contenido como nombre de archivo seguro (sin tildes ni símbolos). */
    static String nombreArchivo(String titulo) {
        String s = Normalizer.normalize(titulo == null ? "contenido" : titulo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9 ._-]", "").trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) s = "contenido";
        return s.length() > 80 ? s.substring(0, 80).trim() : s;
    }

    private byte[] zip(String base, List<String> pistas, double velocidad) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (int i = 0; i < pistas.size(); i++) {
                zip.putNextEntry(new ZipEntry(String.format(Locale.ROOT, "%02d - %s.mp3", i + 1, base)));
                zip.write(voz.sintetizar(pistas.get(i), velocidad));
                zip.closeEntry();
            }
            zip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

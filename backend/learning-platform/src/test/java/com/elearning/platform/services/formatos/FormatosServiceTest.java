package com.elearning.platform.services.formatos;

import com.elearning.platform.entity.*;
import com.elearning.platform.entity.FormatoAlternativo.Estado;
import com.elearning.platform.entity.FormatoAlternativo.Tipo;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.RolUsuario;
import com.elearning.platform.events.FormatoSolicitadoEvent;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.ContenidoRepository;
import com.elearning.platform.repository.FormatoAlternativoRepository;
import com.elearning.platform.repository.RecursoAccesibleRepository;
import com.elearning.platform.security.UsuarioPrincipal;
import com.elearning.platform.services.AuditoriaService;
import com.elearning.platform.services.ContenidoService;
import com.elearning.platform.services.NotificacionService;
import com.elearning.platform.services.voz.TextoAVozPrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-032 (HU-032): pedir, reutilizar, generar en segundo plano, avisar y descargar con auditoría. */
class FormatosServiceTest {

    @TempDir
    Path uploads;

    private final UsuarioPrincipal ana = new UsuarioPrincipal(20L, "ana@tdea.edu.co", "Ana", RolUsuario.ESTUDIANTE, 1L);
    private ContenidoService contenidos;
    private FormatoAlternativoRepository repo;
    private ApplicationEventPublisher eventos;
    private AuditoriaService auditoria;
    private FormatosService servicio;
    private Contenido lectura;
    private FormatoAlternativo guardado;

    @BeforeEach
    void preparar() {
        contenidos = mock(ContenidoService.class);
        repo = mock(FormatoAlternativoRepository.class);
        eventos = mock(ApplicationEventPublisher.class);
        auditoria = mock(AuditoriaService.class);
        RecursoAccesibleRepository recursos = mock(RecursoAccesibleRepository.class);
        servicio = new FormatosService(contenidos, recursos, repo, auditoria, eventos, uploads.toString());
        lectura = contenido(FormatoContenido.LECTURA);
        when(contenidos.accesible(ana, 1L)).thenReturn(lectura);
        when(repo.save(any())).thenAnswer(i -> {
            FormatoAlternativo f = i.getArgument(0);
            if (f.getId() == null) f.setId(99L);
            guardado = f;
            return f;
        });
    }

    private static Contenido contenido(FormatoContenido f) {
        Instructor ins = new Instructor();
        ins.setId(10L);
        Curso curso = new Curso();
        curso.setId(5L);
        curso.setInstructor(ins);
        Modulo m = new Modulo();
        m.setCurso(curso);
        Contenido c = new Contenido();
        c.setId(1L);
        c.setTitulo("Roles de Scrum");
        c.setFormato(f);
        c.setCuerpo("## Roles\n\nEl Product Owner prioriza el backlog.");
        c.setModulo(m);
        return c;
    }

    private FormatoAlternativo listo(Tipo tipo) throws Exception {
        FormatoAlternativo f = new FormatoAlternativo();
        f.setId(7L);
        f.setContenidoId(1L);
        f.setTipo(tipo);
        f.setEstado(Estado.LISTO);
        f.setHuellaContenido(FormatosService.huella(lectura, null));
        f.setRutaArchivo(servicio.guardarArchivo(f, "<html lang=es></html>".getBytes()));
        when(repo.findByContenidoIdAndTipo(1L, tipo)).thenReturn(Optional.of(f));
        return f;
    }

    @Test
    void cadaLecturaOfreceLosCuatroFormatos() {
        var r = servicio.listar(ana, 1L);
        assertFalse(r.soloTranscripcion());
        assertEquals(List.of("MP3", "EPUB", "HTML", "BRF"), r.formatos().stream().map(f -> f.tipo()).toList());
    }

    @Test
    void losVideosSoloOfrecenLaTranscripcion() {
        when(contenidos.accesible(ana, 1L)).thenReturn(contenido(FormatoContenido.VIDEO));
        assertTrue(servicio.listar(ana, 1L).soloTranscripcion());
        ApiException e = assertThrows(ApiException.class, () -> servicio.solicitar(ana, 1L, Tipo.EPUB, "ip"));
        assertEquals("SOLO_TRANSCRIPCION", e.getCodigo());
    }

    @Test
    void pedirUnFormatoNuevoLoGeneraEnSegundoPlano() {
        var v = servicio.solicitar(ana, 1L, Tipo.EPUB, "ip");
        assertEquals("PENDIENTE", v.estado());
        verify(eventos).publishEvent(new FormatoSolicitadoEvent(99L, Tipo.EPUB, 20L));
    }

    @Test
    void unFormatoYaGeneradoSeReutilizaSinVolverAProcesarlo() throws Exception {
        listo(Tipo.HTML);
        var v = servicio.solicitar(ana, 1L, Tipo.HTML, "ip");
        assertEquals("LISTO", v.estado());
        verify(eventos, never()).publishEvent(any());
    }

    @Test
    void siElContenidoCambiaSeGeneraDeNuevo() throws Exception {
        listo(Tipo.HTML);
        lectura.setCuerpo("Texto corregido por el instructor.");
        assertEquals("PENDIENTE", servicio.solicitar(ana, 1L, Tipo.HTML, "ip").estado());
        verify(eventos).publishEvent(any(FormatoSolicitadoEvent.class));
    }

    @Test
    void laDescargaQuedaEnAuditoriaYTardaMenosDeTresSegundos() throws Exception {
        listo(Tipo.HTML);
        long inicio = System.nanoTime();
        var a = servicio.descargar(ana, 1L, Tipo.HTML, "127.0.0.1");
        assertTrue((System.nanoTime() - inicio) / 1_000_000 < 3000);
        assertEquals("Roles de Scrum.html", a.nombre());
        verify(auditoria).registrar(eq(20L), eq("FORMATO_DESCARGADO"), contains("HTML"), eq(ResultadoAuditoria.PERMITIDO), anyString());
    }

    @Test
    void noSeDescargaLoQueNoEstaListo() {
        when(repo.findByContenidoIdAndTipo(1L, Tipo.BRF)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> servicio.descargar(ana, 1L, Tipo.BRF, "ip"));
    }

    @Test
    void laGeneracionGuardaElArchivoYAvisaAlEstudiante() {
        FormatoAlternativo f = new FormatoAlternativo();
        f.setId(99L);
        f.setContenidoId(1L);
        f.setTipo(Tipo.BRF);
        when(repo.findById(99L)).thenReturn(Optional.of(f));
        ContenidoRepository crepo = mock(ContenidoRepository.class);
        when(crepo.findById(1L)).thenReturn(Optional.of(lectura));
        NotificacionService notificaciones = mock(NotificacionService.class);

        new FormatosGeneracion(repo, crepo, servicio, new TextoAVozPrueba(), new BrailleBasico(), notificaciones).generar(99L, 20L);

        assertEquals(Estado.LISTO, f.getEstado());
        assertTrue(Files.exists(uploads.resolve("formatos").resolve(f.getRutaArchivo())));
        verify(notificaciones).notificar(eq(20L), contains("listo"), anyString(), eq(false), eq("/contenidos/1"));
    }

    @Test
    void siLaConversionFallaSeAvisaAlEstudianteYAlInstructor() {
        FormatoAlternativo f = new FormatoAlternativo();
        f.setId(99L);
        f.setContenidoId(1L);
        f.setTipo(Tipo.BRF);
        when(repo.findById(99L)).thenReturn(Optional.of(f));
        ContenidoRepository crepo = mock(ContenidoRepository.class);
        when(crepo.findById(1L)).thenReturn(Optional.of(lectura));
        ServicioBraille roto = mock(ServicioBraille.class);
        when(roto.aBrf(any())).thenThrow(new IllegalStateException("tabla no encontrada"));
        NotificacionService notificaciones = mock(NotificacionService.class);

        new FormatosGeneracion(repo, crepo, servicio, new TextoAVozPrueba(), roto, notificaciones).generar(99L, 20L);

        assertEquals(Estado.FALLIDO, f.getEstado());
        verify(notificaciones).notificar(eq(20L), contains("No pudimos"), contains("otro formato"), eq(false), anyString());
        verify(notificaciones).notificarUnaVezAlDia(eq(10L), contains("Falló"), anyString(), anyString());
    }
}

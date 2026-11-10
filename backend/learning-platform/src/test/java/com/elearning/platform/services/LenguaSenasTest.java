package com.elearning.platform.services;

import com.elearning.platform.entity.Contenido;
import com.elearning.platform.entity.RecursoAccesible;
import com.elearning.platform.enums.FormatoContenido;
import com.elearning.platform.enums.ResultadoAuditoria;
import com.elearning.platform.enums.TipoRecursoAccesible;
import com.elearning.platform.exception.ApiException;
import com.elearning.platform.repository.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RF-040 (HU-040): video de lengua de señas asociado a video o lectura, con desfase ajustable. */
class LenguaSenasTest {

    private static Contenido contenido(FormatoContenido f) {
        Contenido c = new Contenido();
        c.setId(1L);
        c.setFormato(f);
        return c;
    }

    @Test
    void seAsociaAUnVideoOAUnaLectura() {
        assertDoesNotThrow(() -> InstructorCursoService.validarLenguaSenas(contenido(FormatoContenido.VIDEO), 0));
        assertDoesNotThrow(() -> InstructorCursoService.validarLenguaSenas(contenido(FormatoContenido.LECTURA), null));
        ApiException e = assertThrows(ApiException.class,
                () -> InstructorCursoService.validarLenguaSenas(contenido(FormatoContenido.SIMULACION), 0));
        assertEquals("FORMATO_SIN_LENGUA_SENAS", e.getCodigo());
    }

    @Test
    void elDesfaseTieneUnLimiteRazonable() {
        assertDoesNotThrow(() -> InstructorCursoService.validarLenguaSenas(contenido(FormatoContenido.VIDEO), -1500));
        assertThrows(ApiException.class,
                () -> InstructorCursoService.validarLenguaSenas(contenido(FormatoContenido.VIDEO), 700_000));
    }

    @Test
    void elInstructorAjustaElDesfaseYQuedaEnAuditoria() {
        RecursoAccesibleRepository recursos = mock(RecursoAccesibleRepository.class);
        AccesoService acceso = mock(AccesoService.class);
        AuditoriaService auditoria = mock(AuditoriaService.class);
        InstructorCursoService s = new InstructorCursoService(acceso, mock(CursoRepository.class), mock(ModuloRepository.class),
                mock(ContenidoRepository.class), recursos, mock(EvaluacionRepository.class), mock(InstructorRepository.class),
                auditoria, mock(NotificacionService.class), mock(AccesibilidadContenidoService.class), mock(SubtitulosService.class));
        RecursoAccesible r = new RecursoAccesible();
        r.setId(9L);
        r.setTipo(TipoRecursoAccesible.LENGUA_SENAS);
        r.setContenido(contenido(FormatoContenido.VIDEO));
        r.setUrl("https://x/interprete.mp4");
        when(recursos.findById(9L)).thenReturn(Optional.of(r));

        var vista = s.ajustarDesfase(9L, 10L, 1200, "127.0.0.1");

        assertEquals(1200, vista.desfaseMs());
        verify(acceso).contenidoPropio(1L, 10L);
        verify(auditoria).registrar(eq(10L), eq("DESFASE_LENGUA_SENAS"), contains("1200"), eq(ResultadoAuditoria.PERMITIDO), anyString());
    }

    @Test
    void elDesfaseSoloAplicaAlInterprete() {
        RecursoAccesibleRepository recursos = mock(RecursoAccesibleRepository.class);
        InstructorCursoService s = new InstructorCursoService(mock(AccesoService.class), mock(CursoRepository.class),
                mock(ModuloRepository.class), mock(ContenidoRepository.class), recursos, mock(EvaluacionRepository.class),
                mock(InstructorRepository.class), mock(AuditoriaService.class), mock(NotificacionService.class),
                mock(AccesibilidadContenidoService.class), mock(SubtitulosService.class));
        RecursoAccesible r = new RecursoAccesible();
        r.setTipo(TipoRecursoAccesible.SUBTITULO);
        r.setContenido(contenido(FormatoContenido.VIDEO));
        when(recursos.findById(9L)).thenReturn(Optional.of(r));
        assertThrows(ApiException.class, () -> s.ajustarDesfase(9L, 10L, 500, "127.0.0.1"));
    }
}

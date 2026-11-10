package com.elearning.platform.services;

import com.elearning.platform.entity.Notificacion;
import com.elearning.platform.entity.Usuario;
import com.elearning.platform.enums.CanalNotificacion;
import com.elearning.platform.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RF-047: envía el resumen diario de notificaciones no urgentes. Las notificaciones guardadas para
 * el resumen ya quedan visibles a la hora elegida (se posponen hasta esa hora); este proceso, que
 * corre cada 5 minutos (margen de ±5 min), agrega una notificación de resumen y, si la persona lo
 * eligió, le envía un único correo con todas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumenDiarioJob {

    private final NotificacionRepository notificaciones;
    private final MailService correo;

    @Scheduled(cron = "${vlearning.notificaciones.resumen-cron:0 */5 * * * *}")
    @Transactional
    public void enviar() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Notificacion> listas = notificaciones.paraResumen(ahora);
        if (listas.isEmpty()) return;
        Map<Usuario, List<Notificacion>> porUsuario = listas.stream()
                .collect(Collectors.groupingBy(Notificacion::getUsuario, LinkedHashMap::new, Collectors.toList()));
        porUsuario.forEach((u, lista) -> {
            String detalle = textoResumen(lista);
            Notificacion resumen = new Notificacion();
            resumen.setUsuario(u);
            resumen.setTitulo("Tu resumen diario: " + lista.size() + " aviso(s)");
            resumen.setMensaje(detalle);
            resumen.setCanal(u.getCanalPreferido());
            resumen.setEnlace(null);
            notificaciones.save(resumen);
            if (u.getCanalPreferido() == CanalNotificacion.CORREO) {
                correo.enviar(u.getCorreoInstitucional(), "Tu resumen diario de V-Learning", detalle);
            }
            lista.forEach(n -> n.setEnResumen(false));
            notificaciones.saveAll(lista);
        });
        log.info("Resumen diario enviado a {} usuario(s).", porUsuario.size());
    }

    /** RF-047: una línea por aviso, en el orden en que llegaron. */
    static String textoResumen(List<Notificacion> lista) {
        StringBuilder sb = new StringBuilder("Estos son los avisos que juntamos para ti:\n");
        lista.stream().sorted((a, b) -> a.getFechaCreacion().compareTo(b.getFechaCreacion()))
                .forEach(n -> sb.append("\n• ").append(n.getTitulo()).append(": ").append(n.getMensaje()));
        return sb.toString();
    }
}

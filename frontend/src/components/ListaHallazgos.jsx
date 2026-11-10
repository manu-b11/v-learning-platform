import { useState } from "react";
import { CircleAlert, Sparkles, TriangleAlert } from "lucide-react";
import { api, mensajeError } from "../api/client.js";
import { CRITERIOS_WCAG, REGLAS_CON_BORRADOR } from "../utils/wcag.js";
import { Button } from "./ui/index.jsx";

/** RF-039 (criterio 4): pedir el borrador automático de subtítulos desde el informe. */
function GenerarBorrador({ contenidoId, contenido }) {
  const [estado, setEstado] = useState(null);
  const [trabajando, setTrabajando] = useState(false);
  const pedir = async () => {
    setTrabajando(true);
    try {
      await api.instructor.generarSubtitulos(contenidoId);
      setEstado("Pedimos el borrador. Te avisaremos cuando esté listo para revisarlo y aprobarlo.");
    } catch (e) {
      setEstado(mensajeError(e));
    } finally {
      setTrabajando(false);
    }
  };
  return (
    <div className="mt-2 flex flex-wrap items-center gap-3">
      <Button variante="secondary" icono={Sparkles} cargando={trabajando} onClick={pedir} disabled={Boolean(estado)}>
        Generar borrador automático{contenido ? <span className="sr-only"> de «{contenido}»</span> : null}
      </Button>
      <p role="status" aria-live="polite" className="text-sm">{estado}</p>
    </div>
  );
}

/**
 * RF-049: lista de hallazgos de accesibilidad. Cada uno dice, en texto y no solo con color, si
 * bloquea la publicación, qué criterio WCAG incumple y cómo corregirlo.
 */
export default function ListaHallazgos({ hallazgos, mostrarContenido = false, ofrecerBorrador = true }) {
  if (!hallazgos?.length) return null;
  const orden = [...hallazgos].sort((a, b) => (a.severidad === b.severidad ? 0 : a.severidad === "BLOQUEANTE" ? -1 : 1));
  return (
    <ul className="grid gap-3">
      {orden.map((h, i) => {
        const bloquea = h.severidad === "BLOQUEANTE";
        const Icono = bloquea ? CircleAlert : TriangleAlert;
        return (
          <li key={`${h.contenidoId}-${h.regla}-${i}`}
            className={`list-none rounded-opt border p-3 ${bloquea ? "border-err-fg bg-err-bg" : "border-line bg-surface"}`}>
            <p className="flex items-start gap-2 font-bold text-strong">
              <Icono size={18} aria-hidden="true" className="mt-0.5 shrink-0" />
              <span>
                <span className="mr-1 uppercase tracking-wide text-xs">{bloquea ? "Bloquea la publicación:" : "Advertencia:"}</span>
                {mostrarContenido && h.contenido ? `«${h.contenido}»: ` : ""}{h.mensaje}
              </span>
            </p>
            <p className="mt-1 text-sm">
              <strong>Criterio WCAG {h.criterioWcag}</strong>
              {CRITERIOS_WCAG[h.criterioWcag] ? ` (${CRITERIOS_WCAG[h.criterioWcag]})` : ""}.
            </p>
            <p className="mt-1 text-sm"><strong>Cómo corregirlo:</strong> {h.comoCorregir}</p>
            {ofrecerBorrador && h.contenidoId && REGLAS_CON_BORRADOR.has(h.regla) && (
              <GenerarBorrador contenidoId={h.contenidoId} contenido={mostrarContenido ? h.contenido : null} />
            )}
          </li>
        );
      })}
    </ul>
  );
}

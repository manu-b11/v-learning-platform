import { CircleAlert, TriangleAlert } from "lucide-react";
import { CRITERIOS_WCAG } from "../utils/wcag.js";

/**
 * RF-049: lista de hallazgos de accesibilidad. Cada uno dice, en texto y no solo con color, si
 * bloquea la publicación, qué criterio WCAG incumple y cómo corregirlo.
 */
export default function ListaHallazgos({ hallazgos, mostrarContenido = false }) {
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
          </li>
        );
      })}
    </ul>
  );
}

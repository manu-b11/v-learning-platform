import { useEffect, useState } from "react";
import { api, mensajeError } from "../../api/client.js";
import ListaHallazgos from "../../components/ListaHallazgos.jsx";

/**
 * RF-049: revisión de accesibilidad en vivo mientras el instructor escribe. Espera a que deje de
 * escribir (700 ms) y analiza el borrador en el servidor sin guardarlo. El resumen se anuncia en
 * una región aria-live; la lista detalla criterio WCAG y cómo corregir cada problema.
 */
export default function PanelAccesibilidad({ contenidoId, formato, urlRecurso, cuerpo, version }) {
  const [informe, setInforme] = useState(null);
  const [error, setError] = useState(null);
  const [revisando, setRevisando] = useState(false);

  useEffect(() => {
    let activo = true;
    const t = setTimeout(async () => {
      setRevisando(true);
      try {
        const r = await api.instructor.verificarAccesibilidad(contenidoId, {
          formato, urlRecurso: urlRecurso?.trim() || null, cuerpo: cuerpo || null,
        });
        if (activo) { setInforme(r); setError(null); }
      } catch (e) {
        if (activo) setError(mensajeError(e, "No pudimos revisar la accesibilidad en este momento."));
      } finally {
        if (activo) setRevisando(false);
      }
    }, 700);
    return () => { activo = false; clearTimeout(t); };
  }, [contenidoId, formato, urlRecurso, cuerpo, version]);

  let resumen = "";
  if (error) resumen = error;
  else if (informe && informe.hallazgos.length === 0) resumen = "Sin problemas de accesibilidad detectados.";
  else if (informe) {
    resumen = `${informe.bloqueantes} problema(s) que bloquean la publicación y ${informe.advertencias} advertencia(s).`;
  }

  return (
    <section aria-labelledby="h-a11y" className="grid gap-3 rounded-opt border border-line p-4">
      <h3 id="h-a11y" className="text-lg">Revisión de accesibilidad (WCAG 2.2)</h3>
      <p className="text-sm text-muted">
        Se revisa mientras escribes. Lo que bloquea la publicación hay que corregirlo; las advertencias son recomendaciones.
      </p>
      <p role="status" aria-live="polite" className="font-semibold text-strong">
        {revisando && !informe ? "Revisando…" : resumen}
      </p>
      {informe && <ListaHallazgos hallazgos={informe.hallazgos} ofrecerBorrador={false} />}
    </section>
  );
}

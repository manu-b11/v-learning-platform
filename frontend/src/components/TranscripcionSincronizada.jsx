import { useEffect, useId, useRef, useState } from "react";
import { Download, FileText } from "lucide-react";
import { aSrt, aTxt, etiquetaTiempo, lineaActual, parseVtt } from "../utils/vtt.js";
import { Button } from "./ui/index.jsx";
import { cx } from "../utils/cx.js";

function bajar(nombre, texto, tipo) {
  const url = URL.createObjectURL(new Blob([texto], { type: `${tipo};charset=utf-8` }));
  const a = document.createElement("a");
  a.href = url;
  a.download = nombre;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

/**
 * RF-038: transcripción sincronizada junto al video. Sale del mismo VTT de los subtítulos
 * aprobados; resalta la línea en curso (aria-current, borde y negrita, no solo color), cada línea
 * es un botón que lleva el video a ese momento y se puede descargar en .txt o .srt.
 */
export default function TranscripcionSincronizada({ medio, onSaltar, urlVtt, titulo, abiertaAlInicio = false }) {
  const [lineas, setLineas] = useState(null);
  const [fallo, setFallo] = useState(false);
  const [abierta, setAbierta] = useState(abiertaAlInicio);
  const [actual, setActual] = useState(-1);
  const lista = useRef(null);
  const id = useId();

  useEffect(() => {
    let activo = true;
    fetch(urlVtt)
      .then((r) => (r.ok ? r.text() : Promise.reject(new Error(String(r.status)))))
      .then((t) => { if (activo) setLineas(parseVtt(t)); })
      .catch(() => { if (activo) setFallo(true); });
    return () => { activo = false; };
  }, [urlVtt]);

  useEffect(() => {
    const m = medio.current;
    if (!m || !lineas) return undefined;
    const alAvanzar = () => setActual(lineaActual(lineas, m.currentTime));
    m.addEventListener("timeupdate", alAvanzar);
    m.addEventListener("seeked", alAvanzar);
    return () => { m.removeEventListener("timeupdate", alAvanzar); m.removeEventListener("seeked", alAvanzar); };
  }, [medio, lineas]);

  useEffect(() => {
    if (!abierta || actual < 0) return;
    const el = lista.current?.querySelector(`[data-i="${actual}"]`);
    // Desplaza solo la lista, sin mover el foco ni la página.
    if (el && lista.current) {
      const c = lista.current;
      if (el.offsetTop < c.scrollTop || el.offsetTop + el.offsetHeight > c.scrollTop + c.clientHeight) {
        c.scrollTop = el.offsetTop - c.clientHeight / 3;
      }
    }
  }, [actual, abierta]);

  if (fallo) {
    return <p className="text-sm text-muted">No pudimos cargar la transcripción sincronizada. Usa los subtítulos del video.</p>;
  }
  if (!lineas || lineas.length === 0) return null;

  const ir = (l) => {
    onSaltar(l.inicio);
    setActual(lineas.indexOf(l));
  };
  const base = (titulo || "transcripcion").replace(/[\\/:*?"<>|]+/g, "").trim() || "transcripcion";

  return (
    <section aria-labelledby={`${id}-t`} className="grid content-start gap-3">
      <div className="flex flex-wrap items-center gap-2">
        <h2 id={`${id}-t`} className="sr-only">Transcripción sincronizada</h2>
        <Button variante="secondary" icono={FileText} aria-expanded={abierta} aria-controls={`${id}-l`}
          onClick={() => setAbierta((a) => !a)}>
          {abierta ? "Ocultar transcripción" : "Mostrar transcripción"}
        </Button>
        {abierta && (
          <>
            <Button variante="ghost" icono={Download} onClick={() => bajar(`${base}.txt`, aTxt(lineas), "text/plain")}>
              .txt<span className="sr-only"> descargar transcripción en texto</span>
            </Button>
            <Button variante="ghost" icono={Download} onClick={() => bajar(`${base}.srt`, aSrt(lineas), "application/x-subrip")}>
              .srt<span className="sr-only"> descargar transcripción en formato de subtítulos</span>
            </Button>
          </>
        )}
      </div>
      <ol id={`${id}-l`} ref={lista} hidden={!abierta} aria-label="Líneas de la transcripción"
        className="relative max-h-[28rem] overflow-y-auto rounded-card border border-line bg-surface p-2">
        {lineas.map((l, i) => {
          const enCurso = i === actual;
          return (
            <li key={`${l.inicio}-${i}`} data-i={i} className="list-none">
              <button type="button" onClick={() => ir(l)} aria-current={enCurso ? "true" : undefined}
                className={cx("flex min-h-11 w-full gap-3 rounded-lg border-l-4 px-3 py-2 text-left text-strong hover:bg-subtle",
                  enCurso ? "border-primary-600 bg-primary-100 font-bold" : "border-transparent")}>
                <span className="shrink-0 font-mono text-sm tabular-nums text-muted">{etiquetaTiempo(l.inicio)}</span>
                <span>{l.texto}{enCurso && <span className="sr-only"> (en curso)</span>}</span>
              </button>
            </li>
          );
        })}
      </ol>
    </section>
  );
}

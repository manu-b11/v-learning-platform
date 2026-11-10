import { useEffect, useRef, useState } from "react";
import { Download, FileDown, RefreshCw } from "lucide-react";
import { api, mensajeError } from "../api/client.js";
import { Banner, Button, Cargando } from "./ui/index.jsx";

const ESTADO = {
  SIN_GENERAR: "Aún no generado",
  PENDIENTE: "En cola para generarse…",
  GENERANDO: "Generándose…",
  LISTO: "Listo para descargar",
  FALLIDO: "No se pudo generar",
};

const kb = (b) => (b ? ` · ${Math.max(1, Math.round(b / 1024))} KB` : "");

/**
 * RF-032: descargar el contenido en MP3, EPUB, HTML accesible o braille (BRF). Lo ya generado se
 * descarga al momento; lo nuevo se genera en segundo plano y avisamos al terminar (también con
 * una notificación). Videos y simulaciones solo ofrecen su transcripción.
 */
export default function FormatosAlternativos({ contenidoId }) {
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(null);
  const [trabajando, setTrabajando] = useState(null);
  const [anuncio, setAnuncio] = useState("");
  const previos = useRef({});

  const cargar = async () => {
    try {
      const r = await api.formatos.listar(contenidoId);
      r.formatos.forEach((f) => {
        if (previos.current[f.tipo] && previos.current[f.tipo] !== "LISTO" && f.estado === "LISTO") {
          setAnuncio(`${f.nombre} listo para descargar.`);
        }
        previos.current[f.tipo] = f.estado;
      });
      setDatos(r);
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  useEffect(() => { cargar(); }, [contenidoId]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!datos?.formatos.some((f) => ["PENDIENTE", "GENERANDO"].includes(f.estado))) return undefined;
    const t = setTimeout(cargar, 3000);
    return () => clearTimeout(t);
  }, [datos]); // eslint-disable-line react-hooks/exhaustive-deps

  if (error && !datos) return <Banner tono="err">{error}</Banner>;
  if (!datos) return <Cargando texto="Cargando formatos…" />;
  if (datos.soloTranscripcion) return <Banner tono="info">{datos.mensaje}</Banner>;

  const pedir = async (f) => {
    setTrabajando(f.tipo);
    setError(null);
    try {
      const v = await api.formatos.solicitar(contenidoId, f.tipo);
      setAnuncio(v.estado === "LISTO" ? `${f.nombre} listo para descargar.` : `Generando ${f.nombre}. Te avisaremos cuando esté listo.`);
      await cargar();
    } catch (e) {
      setError(mensajeError(e));
    } finally {
      setTrabajando(null);
    }
  };
  const bajar = async (f) => {
    setTrabajando(f.tipo);
    setError(null);
    try {
      await api.formatos.descargar(contenidoId, f.tipo);
      setAnuncio(`Descargaste ${f.nombre}.`);
    } catch (e) {
      setError(mensajeError(e));
    } finally {
      setTrabajando(null);
    }
  };

  return (
    <div className="grid gap-3">
      <p className="text-body">Elige un formato para estudiar con tu tecnología de apoyo o sin conexión.</p>
      {error && <Banner tono="err">{error}</Banner>}
      <ul className="grid gap-2">
        {datos.formatos.map((f) => {
          const generando = ["PENDIENTE", "GENERANDO"].includes(f.estado);
          return (
            <li key={f.tipo} className="flex list-none flex-wrap items-center justify-between gap-3 rounded-opt border border-line p-3">
              <div>
                <p className="font-bold text-strong">{f.nombre}</p>
                <p className="text-sm">{ESTADO[f.estado]}{f.estado === "LISTO" ? kb(f.tamanoBytes) : ""}</p>
                {f.mensaje && <p className="text-sm font-semibold text-strong">{f.mensaje}</p>}
              </div>
              {f.estado === "LISTO" ? (
                <Button icono={Download} cargando={trabajando === f.tipo} onClick={() => bajar(f)}>
                  Descargar<span className="sr-only"> {f.nombre}</span>
                </Button>
              ) : (
                <Button variante="secondary" icono={f.estado === "FALLIDO" ? RefreshCw : FileDown} disabled={generando}
                  cargando={trabajando === f.tipo} onClick={() => pedir(f)}
                  motivo={generando ? "Se está generando; te avisaremos." : undefined}>
                  {f.estado === "FALLIDO" ? "Intentar de nuevo" : "Generar"}<span className="sr-only"> {f.nombre}</span>
                </Button>
              )}
            </li>
          );
        })}
      </ul>
      <p className="sr-only" role="status" aria-live="polite">{anuncio}</p>
    </div>
  );
}

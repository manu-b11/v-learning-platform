import { useEffect, useRef, useState } from "react";
import { CheckCircle2, Plus, Sparkles, Trash2, Upload } from "lucide-react";
import { api, mensajeError } from "../../api/client.js";
import { useNotif } from "../../context/NotifContext.jsx";
import { Banner, Button, TextField } from "../../components/ui/index.jsx";
import { aSegundos, formatoSrt } from "../../utils/vtt.js";

/** 75500 ms → "00:01:15.500" (formato que acepta el editor). */
const aMarca = (ms) => formatoSrt(ms / 1000).replace(",", ".");
const aMs = (texto) => {
  const s = aSegundos(texto ?? "");
  return s === null ? null : Math.round(s * 1000);
};

const ESTADOS = {
  SIN_BORRADOR: "Todavía no hay borrador de subtítulos.",
  PENDIENTE: "Generando el borrador en segundo plano. Te avisaremos cuando esté listo.",
  GENERANDO: "Generando el borrador en segundo plano. Te avisaremos cuando esté listo.",
  LISTO: "Borrador listo para revisar: corrige el texto y los tiempos y apruébalo. Hasta entonces el contenido no se publica.",
  APROBADO: "Subtítulos aprobados: ya forman parte del contenido.",
};

/**
 * RF-037: borrador de subtítulos automáticos para un video o podcast. El instructor lo genera,
 * lo corrige línea por línea (texto y marcas de tiempo) o sube un .srt/.vtt, y lo aprueba. Los
 * subtítulos automáticos nunca se publican sin esa aprobación.
 */
export default function SubtitulosBorrador({ contenidoId, onAprobado }) {
  const { avisar } = useNotif();
  const [b, setB] = useState(null);
  const [lineas, setLineas] = useState([]);
  const [cambios, setCambios] = useState(false);
  const [error, setError] = useState(null);
  const [trabajando, setTrabajando] = useState(null);
  const archivo = useRef(null);

  const cargar = async () => {
    try {
      const r = await api.instructor.subtitulos(contenidoId);
      setB(r);
      setLineas(r.lineas.map((l) => ({ inicio: aMarca(l.inicioMs), fin: aMarca(l.finMs), texto: l.texto })));
      setCambios(false);
      return r;
    } catch (e) {
      setError(mensajeError(e));
      return null;
    }
  };

  useEffect(() => {
    cargar();
  }, [contenidoId]); // eslint-disable-line react-hooks/exhaustive-deps

  // Mientras se genera, se consulta cada 3 s.
  useEffect(() => {
    if (!b || !["PENDIENTE", "GENERANDO"].includes(b.estado)) return undefined;
    const t = setTimeout(cargar, 3000);
    return () => clearTimeout(t);
  }, [b]); // eslint-disable-line react-hooks/exhaustive-deps

  const accion = async (nombre, fn, ok) => {
    setTrabajando(nombre);
    setError(null);
    try {
      const r = await fn();
      setB(r);
      setLineas(r.lineas.map((l) => ({ inicio: aMarca(l.inicioMs), fin: aMarca(l.finMs), texto: l.texto })));
      setCambios(false);
      if (ok) avisar(ok, "ok");
      return r;
    } catch (e) {
      setError(mensajeError(e));
      return null;
    } finally {
      setTrabajando(null);
    }
  };

  const generar = () => accion("generar", () => api.instructor.generarSubtitulos(contenidoId), "Pedimos el borrador de subtítulos.");
  const guardar = () => {
    const datos = lineas.map((l) => ({ inicioMs: aMs(l.inicio), finMs: aMs(l.fin), texto: l.texto }));
    const mala = datos.findIndex((l) => l.inicioMs === null || l.finMs === null);
    if (mala >= 0) {
      setError(`Línea ${mala + 1}: escribe los tiempos como 00:01:15.500.`);
      return;
    }
    accion("guardar", () => api.instructor.editarSubtitulos(contenidoId, datos), "Guardamos los cambios del borrador.");
  };
  const aprobar = async () => {
    const r = await accion("aprobar", () => api.instructor.aprobarSubtitulos(contenidoId), "Subtítulos aprobados.");
    if (r) onAprobado?.();
  };
  const subir = () => {
    const f = archivo.current?.files?.[0];
    if (!f) { setError("Selecciona un archivo .srt o .vtt."); return; }
    accion("subir", () => api.instructor.subirSubtitulos(contenidoId, f), "Cargamos el archivo como borrador.");
  };

  const cambiar = (i, campo, valor) => {
    setLineas((ls) => ls.map((l, j) => (j === i ? { ...l, [campo]: valor } : l)));
    setCambios(true);
  };
  const quitar = (i) => { setLineas((ls) => ls.filter((_, j) => j !== i)); setCambios(true); };
  const agregar = () => {
    const ultima = lineas[lineas.length - 1];
    const inicio = ultima ? aMs(ultima.fin) ?? 0 : 0;
    setLineas((ls) => [...ls, { inicio: aMarca(inicio), fin: aMarca(inicio + 3000), texto: "" }]);
    setCambios(true);
  };

  if (!b) return null;
  const editable = ["LISTO", "APROBADO"].includes(b.estado) || (b.estado === "FALLIDO" && lineas.length > 0);
  const generando = ["PENDIENTE", "GENERANDO"].includes(b.estado);

  return (
    <section aria-labelledby="h-sub" className="mt-6 grid gap-3 border-t border-line pt-5">
      <h3 id="h-sub" className="text-xl">Subtítulos automáticos</h3>
      <p role="status" aria-live="polite" className="font-semibold text-strong">
        {b.estado === "FALLIDO" ? b.error : ESTADOS[b.estado]}
        {b.estado === "LISTO" && b.origen === "AUTOMATICO" && " Revisa con cuidado: el reconocimiento de voz comete errores."}
      </p>
      {b.medioCambio && <Banner tono="warn">El enlace del medio cambió después de generar este borrador. Genera uno nuevo.</Banner>}
      {error && <Banner tono="err">{error}</Banner>}
      <div className="flex flex-wrap gap-3">
        <Button variante="secondary" icono={Sparkles} disabled={generando} cargando={trabajando === "generar"} onClick={generar}
          motivo={generando ? "Ya se está generando un borrador." : undefined}>
          {b.estado === "SIN_BORRADOR" ? "Generar borrador automático" : "Generar un borrador nuevo"}
        </Button>
      </div>

      {editable && (
        <div className="grid gap-3">
          <ol className="grid gap-3" aria-label="Líneas de subtítulos">
            {lineas.map((l, i) => (
              <li key={i} className="grid list-none gap-2 rounded-opt border border-line p-3 sm:grid-cols-[9rem_9rem_1fr_auto] sm:items-end">
                <TextField label={`Inicio de la línea ${i + 1}`} value={l.inicio} onChange={(e) => cambiar(i, "inicio", e.target.value)}
                  inputMode="decimal" className="[&_label]:text-sm" />
                <TextField label={`Fin de la línea ${i + 1}`} value={l.fin} onChange={(e) => cambiar(i, "fin", e.target.value)}
                  inputMode="decimal" className="[&_label]:text-sm" />
                <TextField multilinea rows={2} label={`Texto de la línea ${i + 1}`} value={l.texto} maxLength={500}
                  onChange={(e) => cambiar(i, "texto", e.target.value)} className="[&_label]:text-sm [&_textarea]:min-h-12" />
                <Button variante="ghost" icono={Trash2} onClick={() => quitar(i)}>
                  <span className="sr-only">Quitar la línea {i + 1}</span>
                </Button>
              </li>
            ))}
          </ol>
          <div className="flex flex-wrap gap-3">
            <Button variante="secondary" icono={Plus} onClick={agregar}>Agregar línea</Button>
            <Button variante="secondary" disabled={!cambios} cargando={trabajando === "guardar"} onClick={guardar}
              motivo={!cambios ? "No hay cambios por guardar." : undefined}>Guardar subtítulos</Button>
            <Button icono={CheckCircle2} disabled={b.estado !== "LISTO" || cambios} cargando={trabajando === "aprobar"} onClick={aprobar}
              motivo={cambios ? "Guarda los cambios antes de aprobar." : b.estado === "APROBADO" ? "Ya están aprobados." : undefined}>
              Aprobar subtítulos
            </Button>
          </div>
        </div>
      )}

      <div className="grid gap-2 rounded-opt bg-subtle p-3">
        <label htmlFor={`sub-${contenidoId}`} className="font-bold text-strong">
          ¿El reconocimiento falló o ya tienes subtítulos? Sube un archivo .srt o .vtt
        </label>
        <div className="flex flex-wrap items-center gap-3">
          <input id={`sub-${contenidoId}`} ref={archivo} type="file" accept=".srt,.vtt" className="min-h-11 max-w-full text-sm" />
          <Button variante="secondary" icono={Upload} cargando={trabajando === "subir"} onClick={subir}>Subir archivo</Button>
        </div>
      </div>
    </section>
  );
}

import { useCallback, useEffect, useRef, useState } from "react";
import {
  Link,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { Check, Clock, Flag, Pause, Play, Send, Trash2, Wifi, WifiOff } from "lucide-react";
import { api, mensajeError, token } from "../../api/client.js";
import { useAuth } from "../../context/AuthContext.jsx";
import { useNotif } from "../../context/NotifContext.jsx";
import { usePrefs } from "../../context/PrefsContext.jsx";
import { useAsync } from "../../utils/useAsync.js";
import { ROL_TEXTO, fmtFecha, fmtHora, fmtDia } from "../../utils/format.js";
import {
  Badge,
  Banner,
  Button,
  Card,
  Cargando,
  EstadoError,
  PageHeader,
  Pestanas,
  SelectField,
  TextField,
  Toggle,
  Vacio,
} from "../../components/ui/index.jsx";
import ConfirmarDialogo from "../../components/ConfirmarDialogo.jsx";
import VolverACursos from "../../components/VolverACursos.jsx";
import BotonDictado from "../../components/BotonDictado.jsx";

function cursosDelRol(rol) {
  if (rol === "ESTUDIANTE") return api.cursos.mios();
  if (rol === "INSTRUCTOR") return api.instructor.cursos();
  return api.admin.cursos();
}

/** Identificador del mensaje generado en el navegador: evita duplicados al reenviar desde la cola. */
function nuevoIdCliente() {
  try {
    return crypto.randomUUID();
  } catch {
    return `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }
}

/** Error sin respuesta del servidor (sin conexión o servidor caído): el mensaje puede ir a la cola. */
const esFallaDeRed = (err) => !err?.response;

/** Cola de mensajes pendientes por usuario y curso (RF-010). Vive mientras dure la sesión del navegador. */
function leerCola(clave) {
  try {
    return JSON.parse(sessionStorage.getItem(clave) ?? "[]");
  } catch {
    return [];
  }
}
function guardarCola(clave, cola) {
  try {
    if (cola.length) sessionStorage.setItem(clave, JSON.stringify(cola));
    else sessionStorage.removeItem(clave);
  } catch {
    /* sin almacenamiento: la cola queda solo en memoria */
  }
}

function Sala({ curso, onModerado }) {
  const { usuario } = useAuth();
  const { avisar } = useNotif();
  const [mensajes, setMensajes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [texto, setTexto] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [conectado, setConectado] = useState(false);
  const [aviso, setAviso] = useState(null);
  const [aEliminar, setAEliminar] = useState(null);
  const fin = useRef(null);
  // RF-034: anuncio de mensajes nuevos sin mover el foco, pausa con contador y preferencia del perfil.
  const { config: prefs } = usePrefs();
  const anunciar = prefs?.anunciarMensajes !== false;
  const anunciarRef = useRef(anunciar);
  const [anuncio, setAnuncio] = useState(null);
  const [pausado, setPausado] = useState(false);
  const pausadoRef = useRef(false);
  const enPausa = useRef([]);
  const [pendientes, setPendientes] = useState(0);
  useEffect(() => { anunciarRef.current = anunciar; }, [anunciar]);
  const anunciarTexto = useCallback((texto) => {
    if (anunciarRef.current) setAnuncio({ id: Date.now() + Math.random(), texto });
  }, []);
  const puedeModerar = usuario.rol !== "ESTUDIANTE";
  // RF-010: cola de reenvío. El instructor decide si está activa en su curso.
  const claveCola = `vl.chat.cola.${usuario.id}.${curso}`;
  const [config, setConfig] = useState({ colaReenvio: true, puedeConfigurar: false });
  const [cola, setColaEstado] = useState(() => leerCola(claveCola));
  const colaRef = useRef(cola);
  const configRef = useRef(config);
  const vaciando = useRef(false);
  const setCola = useCallback(
    (fn) => {
      const nueva = typeof fn === "function" ? fn(colaRef.current) : fn;
      colaRef.current = nueva;
      guardarCola(claveCola, nueva);
      setColaEstado(nueva);
    },
    [claveCola],
  );
  useEffect(() => {
    configRef.current = config;
  }, [config]);

  const agregar = useCallback(
    (m) => setMensajes((l) => (l.some((x) => x.id === m.id) ? l : [...l, m])),
    [],
  );

  useEffect(() => {
    let activo = true;
    setCargando(true);
    setError(null);
    setMensajes([]);
    api.chat
      .historial(curso, 80)
      .then((h) => {
        if (activo) setMensajes(h);
      })
      .catch((e) => {
        if (activo) setError(mensajeError(e));
      })
      .finally(() => {
        if (activo) setCargando(false);
      });
    return () => {
      activo = false;
    };
  }, [curso]);

  useEffect(() => {
    let activo = true;
    api.chat
      .configuracion(curso)
      .then((c) => {
        if (activo) setConfig(c);
      })
      .catch(() => {
        /* si falla se usa la configuración por defecto (cola activa) */
      });
    return () => {
      activo = false;
    };
  }, [curso]);

  /** Envía en orden los mensajes en cola. Se detiene en la primera falla de red. */
  const vaciarCola = useCallback(
    async (forzar = false) => {
      if (vaciando.current || colaRef.current.length === 0) return;
      if (!forzar && !configRef.current.colaReenvio) return;
      vaciando.current = true;
      try {
        while (colaRef.current.length > 0) {
          const item = colaRef.current[0];
          try {
            agregar(await api.chat.enviar(curso, item.contenido, item.idCliente));
            setCola((l) => l.filter((x) => x.idCliente !== item.idCliente));
          } catch (err) {
            if (esFallaDeRed(err)) break;
            // El servidor lo rechazó (p. ej. contenido no permitido): sale de la cola y se avisa.
            setCola((l) => l.filter((x) => x.idCliente !== item.idCliente));
            avisar(`No se envió «${item.contenido.slice(0, 40)}»: ${mensajeError(err)}`, "error");
          }
        }
        if (colaRef.current.length === 0) setAviso("Se enviaron los mensajes que estaban en cola.");
      } finally {
        vaciando.current = false;
      }
    },
    [curso, agregar, setCola, avisar],
  );

  // Reintenta la cola al volver la conexión del navegador y cada 15 s mientras haya pendientes.
  useEffect(() => {
    const alVolver = () => vaciarCola();
    window.addEventListener("online", alVolver);
    const intervalo = setInterval(() => {
      if (colaRef.current.length) vaciarCola();
    }, 15000);
    vaciarCola();
    return () => {
      window.removeEventListener("online", alVolver);
      clearInterval(intervalo);
    };
  }, [vaciarCola]);

  // Tiempo real por WebSocket. Si no conecta, la pantalla sigue funcionando por la API normal.
  useEffect(() => {
    let ws;
    let cerrado = false;
    let reintento;
    const conectar = () => {
      const proto = window.location.protocol === "https:" ? "wss" : "ws";
      ws = new WebSocket(
        `${proto}://${window.location.host}/ws/cursos/${curso}?token=${encodeURIComponent(token.get() ?? "")}`,
      );
      ws.onopen = () => {
        setConectado(true);
        vaciarCola();
      };
      ws.onclose = () => {
        setConectado(false);
        if (!cerrado) reintento = setTimeout(conectar, 5000);
      };
      ws.onmessage = (ev) => {
        try {
          const d = JSON.parse(ev.data);
          if (d.tipo === "MENSAJE") {
            const ajeno = d.mensaje.usuarioId !== usuario.id;
            if (ajeno && pausadoRef.current) {
              // RF-034: en pausa no se insertan mensajes; se cuentan y se muestran al reanudar.
              enPausa.current.push(d.mensaje);
              setPendientes(enPausa.current.length);
            } else {
              agregar(d.mensaje);
              if (ajeno) anunciarTexto(d.mensaje.textoPlano ?? `${d.mensaje.autor}: ${d.mensaje.contenido}`);
            }
          }
          if (d.tipo === "ERROR") setAviso(d.mensaje);
        } catch {
          /* mensaje ilegible */
        }
      };
    };
    conectar();
    return () => {
      cerrado = true;
      clearTimeout(reintento);
      ws?.close();
    };
  }, [curso, usuario.id, agregar, vaciarCola, anunciarTexto]);

  const cambiarPausa = () => {
    if (!pausadoRef.current) {
      pausadoRef.current = true;
      setPausado(true);
      return;
    }
    pausadoRef.current = false;
    setPausado(false);
    const lista = enPausa.current;
    enPausa.current = [];
    setPendientes(0);
    lista.forEach((m) => agregar(m));
    if (lista.length > 0) anunciarTexto(`Se mostraron ${lista.length} mensaje(s) que llegaron en pausa.`);
  };

  useEffect(() => {
    fin.current?.scrollIntoView({ block: "end" });
  }, [mensajes.length]);

  const enviar = async (e) => {
    e.preventDefault();
    const contenido = texto.trim();
    if (!contenido) return;
    setEnviando(true);
    setAviso(null);
    const idCliente = nuevoIdCliente();
    try {
      agregar(await api.chat.enviar(curso, contenido, idCliente));
      setTexto("");
    } catch (err) {
      if (esFallaDeRed(err) && config.colaReenvio) {
        setCola((l) => [...l, { idCliente, contenido, fecha: new Date().toISOString() }]);
        setTexto("");
        setAviso("Sin conexión: tu mensaje quedó en cola y se enviará solo cuando vuelva la conexión.");
      } else if (esFallaDeRed(err)) {
        setAviso("Sin conexión: el mensaje no se envió. Tu texto sigue en el campo para que lo envíes de nuevo.");
      } else {
        setAviso(mensajeError(err));
      }
    } finally {
      setEnviando(false);
    }
  };
  const cambiarCola = async (activa) => {
    try {
      setConfig(await api.chat.cambiarConfiguracion(curso, activa));
      avisar(
        activa
          ? "Cola de reenvío activada: los mensajes sin conexión se enviarán al reconectar."
          : "Cola de reenvío desactivada: si falla la conexión, el mensaje no se envía y queda en el campo.",
        "ok",
      );
    } catch (err) {
      avisar(mensajeError(err), "error");
    }
  };
  // Reportar NO borra el mensaje: queda «en revisión» hasta que el instructor decida.
  const reportar = async (m) => {
    try {
      const r = await api.chat.reportar(m.id);
      setMensajes((l) =>
        l.map((x) =>
          x.id === m.id && x.estado === "ENVIADO"
            ? { ...x, estado: "REPORTADO" }
            : x,
        ),
      );
      avisar(r.mensaje, "ok");
    } catch (e) {
      avisar(mensajeError(e), "error");
    }
  };
  const eliminar = async (m) => {
    await api.chat.ocultar(m.id);
    setMensajes((l) => l.filter((x) => x.id !== m.id));
    avisar("El mensaje se eliminó.", "ok");
    onModerado?.();
  };
  const mantener = async (m) => {
    try {
      const r = await api.chat.mantener(m.id);
      setMensajes((l) =>
        l.map((x) => (x.id === m.id ? { ...x, estado: "ENVIADO" } : x)),
      );
      avisar(r.mensaje, "ok");
      onModerado?.();
    } catch (e) {
      avisar(mensajeError(e), "error");
    }
  };

  if (cargando) return <Cargando texto="Cargando la conversación…" />;
  if (error) return <EstadoError mensaje={error} />;

  return (
    <Card className="flex flex-col gap-4">
      <p className="flex items-center gap-2 text-sm font-bold" role="status">
        {conectado ? (
          <>
            <Wifi size={16} aria-hidden="true" className="text-ok-fg" />
            Conversación en tiempo real
          </>
        ) : (
          <>
            <WifiOff size={16} aria-hidden="true" />
            Sin conexión en vivo; los mensajes se envían igual
          </>
        )}
      </p>
      {/* RF-034: pausar las actualizaciones automáticas (WCAG 2.2.2) y contador de pendientes. */}
      <div className="flex flex-wrap items-center gap-3">
        <Button variante="secondary" icono={pausado ? Play : Pause} aria-pressed={pausado} onClick={cambiarPausa}>
          {pausado ? "Reanudar actualizaciones" : "Pausar actualizaciones"}
        </Button>
        {pausado && (
          <p className="text-sm font-bold text-strong">
            {pendientes === 0 ? "En pausa: no hay mensajes nuevos." : `En pausa: ${pendientes} mensaje(s) nuevo(s) sin mostrar.`}
          </p>
        )}
      </div>
      {/* RF-034: anuncio de cada mensaje nuevo («autor: texto») sin mover el foco. */}
      <div className="sr-only" aria-live="polite" aria-atomic="false">
        {anuncio && <p key={anuncio.id}>{anuncio.texto}</p>}
      </div>
      <div
        className="h-[max(26rem,calc(100dvh-24rem))] overflow-y-auto rounded-opt bg-subtle p-4"
        tabIndex={0}
        role="log"
        aria-label="Mensajes del curso"
        aria-live="off"
      >
        {mensajes.length === 0 && (
          <p>Aún no hay mensajes. Escribe el primero.</p>
        )}
        <ul className="grid gap-3">
          {mensajes.map((m, i) => {
            const dia = fmtDia(m.fechaEnvio);
            const separador =
              i === 0 || dia !== fmtDia(mensajes[i - 1].fechaEnvio);
            const mio = m.usuarioId === usuario.id;
            return (
              <li key={m.id} className="list-none">
                {separador && (
                  <p className="mb-2 text-center text-xs font-bold uppercase text-muted">
                    {dia}
                  </p>
                )}
                <div
                  className={`rounded-opt border p-3 ${mio ? "border-primary-300 bg-primary-100" : "border-line bg-surface"} ${m.estado === "REPORTADO" ? "border-warn-fg" : ""}`}
                >
                  <div className="flex flex-wrap items-center justify-between gap-x-3">
                    <p className="flex flex-wrap items-center gap-2 text-sm">
                      <strong className="text-strong">
                        {mio ? "Tú" : m.autor}
                      </strong>
                      {m.rol !== "ESTUDIANTE" && (
                        <Badge tono="tutor">{ROL_TEXTO[m.rol]}</Badge>
                      )}
                      <span>{fmtHora(m.fechaEnvio)}</span>
                      {m.estado === "REPORTADO" && (
                        <Badge tono="warn">Reportado · en revisión</Badge>
                      )}
                    </p>
                    <div className="flex flex-wrap gap-1">
                      {!mio && m.estado !== "REPORTADO" && (
                        <Button
                          variante="ghost"
                          icono={Flag}
                          onClick={() => reportar(m)}
                        >
                          Reportar
                          <span className="sr-only"> mensaje de {m.autor}</span>
                        </Button>
                      )}
                      {puedeModerar && m.estado === "REPORTADO" && (
                        <Button
                          variante="ghost"
                          icono={Check}
                          onClick={() => mantener(m)}
                        >
                          Mantener
                          <span className="sr-only"> mensaje de {m.autor}</span>
                        </Button>
                      )}
                      {puedeModerar && (
                        <Button
                          variante="ghost"
                          icono={Trash2}
                          onClick={() => setAEliminar(m)}
                        >
                          Eliminar
                          <span className="sr-only"> mensaje de {m.autor}</span>
                        </Button>
                      )}
                    </div>
                  </div>
                  <p className="whitespace-pre-wrap break-words text-strong">
                    {m.contenido}
                  </p>
                </div>
              </li>
            );
          })}
        </ul>
        {cola.length > 0 && (
          <ul className="mt-3 grid gap-3" aria-label="Mensajes en cola">
            {cola.map((m) => (
              <li
                key={m.idCliente}
                className="list-none rounded-opt border border-dashed border-primary-300 bg-primary-100 p-3"
              >
                <p className="flex flex-wrap items-center gap-2 text-sm">
                  <strong className="text-strong">Tú</strong>
                  <Badge tono="warn" icono={Clock}>
                    {config.colaReenvio
                      ? "En cola · se enviará al reconectar"
                      : "No enviado"}
                  </Badge>
                </p>
                <p className="whitespace-pre-wrap break-words text-strong">
                  {m.contenido}
                </p>
                <div className="mt-2 flex flex-wrap gap-1">
                  <Button
                    variante="ghost"
                    icono={Send}
                    onClick={() => vaciarCola(true)}
                  >
                    Enviar ahora
                  </Button>
                  <Button
                    variante="ghost"
                    icono={Trash2}
                    onClick={() =>
                      setCola((l) =>
                        l.filter((x) => x.idCliente !== m.idCliente),
                      )
                    }
                  >
                    Descartar
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
        <div ref={fin} />
      </div>
      {aviso && (
        <Banner tono="info" rol="status">
          {aviso}
        </Banner>
      )}
      {config.puedeConfigurar && (
        <Toggle
          checked={config.colaReenvio}
          onChange={cambiarCola}
          label="Cola de reenvío de mensajes"
          descripcion="Si alguien pierde la conexión, su mensaje queda en cola y se envía solo al reconectar. Desactívala si prefieres que el mensaje no se envíe."
        />
      )}
      <form
        onSubmit={enviar}
        className="flex flex-wrap items-end gap-3"
        noValidate
      >
        <TextField
          className="min-w-60 flex-1"
          label="Escribe un mensaje"
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          maxLength={1000}
        />
        {/* RF-045: dictar el mensaje por voz */}
        <BotonDictado valor={texto} onCambio={(v) => setTexto(v.slice(0, 1000))} campo="en el mensaje" className="mt-0" />
        <Button
          type="submit"
          icono={Send}
          cargando={enviando}
          disabled={!texto.trim()}
          aria-describedby={!texto.trim() ? "motivo-enviar" : undefined}
        >
          Enviar
        </Button>
        {!texto.trim() && (
          <p id="motivo-enviar" className="basis-full text-sm text-muted">
            Escribe un mensaje para poder enviarlo.
          </p>
        )}
      </form>
      <ConfirmarDialogo
        abierto={Boolean(aEliminar)}
        titulo="¿Eliminar este mensaje?"
        textoConfirmar="Sí, eliminar"
        onCerrar={() => setAEliminar(null)}
        onConfirmar={() => eliminar(aEliminar)}
      >
        <p>
          El mensaje de {aEliminar?.autor} dejará de verse en la conversación.
          Esta acción no se puede deshacer.
        </p>
      </ConfirmarDialogo>
    </Card>
  );
}

/** Sección «Mensajes reportados» del instructor: todos los cursos, él decide si se elimina o se mantiene. */
function Reportados({ onCambio }) {
  const { avisar } = useNotif();
  const { datos, cargando, error, recargar } = useAsync(
    () => api.chat.reportados(),
    [],
  );
  const [aEliminar, setAEliminar] = useState(null);

  if (cargando && !datos)
    return <Cargando texto="Cargando los mensajes reportados…" />;
  if (error && !datos)
    return <EstadoError mensaje={error} onReintentar={recargar} />;

  const cambio = async () => {
    await recargar();
    onCambio?.();
  };
  const eliminar = async (m) => {
    await api.chat.ocultar(m.id);
    avisar("El mensaje se eliminó.", "ok");
    await cambio();
  };
  const mantener = async (m) => {
    try {
      const r = await api.chat.mantener(m.id);
      avisar(r.mensaje, "ok");
      await cambio();
    } catch (e) {
      avisar(mensajeError(e), "error");
    }
  };

  if (datos.length === 0)
    return (
      <Vacio titulo="No hay mensajes reportados">
        Cuando alguien reporte un mensaje aparecerá aquí para que decidas si se
        elimina o se mantiene.
      </Vacio>
    );
  return (
    <section aria-labelledby="rep-titulo" className="grid gap-3">
      <h2 id="rep-titulo" className="text-xl">
        Mensajes reportados ({datos.length})
      </h2>
      <Banner tono="info">
        El mensaje sigue visible, marcado «en revisión», hasta que decidas. Tú
        decides qué se elimina y qué se mantiene.
      </Banner>
      <ul className="grid gap-3">
        {datos.map((m) => (
          <Card as="li" key={m.id} className="list-none">
            <p className="flex flex-wrap items-center gap-2 text-sm">
              <Badge tono="warn">En revisión</Badge>
              <strong className="text-strong">{m.curso}</strong>
              <Link
                className="font-bold text-primary-600 underline"
                to={`/comunidad/${m.cursoId}`}
              >
                Ver conversación<span className="sr-only"> de {m.curso}</span>
              </Link>
            </p>
            <p className="mt-2 text-sm">
              <strong className="text-strong">{m.autor}</strong> ·{" "}
              {ROL_TEXTO[m.rolAutor]} · enviado {fmtFecha(m.fechaEnvio)}
            </p>
            <blockquote className="mt-1 whitespace-pre-wrap break-words rounded-opt border border-line bg-subtle p-3 text-strong">
              {m.contenido}
            </blockquote>
            <p className="mt-2 text-sm">
              Reportado por{" "}
              {m.reportadoPor.length ? m.reportadoPor.join(", ") : "—"} ·{" "}
              {fmtFecha(m.fechaReporte)}
            </p>
            <div className="mt-3 flex flex-wrap gap-2">
              <Button icono={Trash2} onClick={() => setAEliminar(m)}>
                Eliminar mensaje<span className="sr-only"> de {m.autor}</span>
              </Button>
              <Button
                variante="secondary"
                icono={Check}
                onClick={() => mantener(m)}
              >
                Mantener mensaje<span className="sr-only"> de {m.autor}</span>
              </Button>
            </div>
          </Card>
        ))}
      </ul>
      <ConfirmarDialogo
        abierto={Boolean(aEliminar)}
        titulo="¿Eliminar este mensaje?"
        textoConfirmar="Sí, eliminar"
        onCerrar={() => setAEliminar(null)}
        onConfirmar={() => eliminar(aEliminar)}
      >
        <p>
          El mensaje de {aEliminar?.autor} dejará de verse en la conversación.
          Esta acción no se puede deshacer.
        </p>
      </ConfirmarDialogo>
    </section>
  );
}

/** RF-010: comunidad del curso (chat). */
export default function Comunidad() {
  const { cursoId } = useParams();
  const nav = useNavigate();
  const { usuario } = useAuth();
  const { datos, cargando, error, recargar } = useAsync(
    () => cursosDelRol(usuario.rol),
    [usuario.rol],
  );
  const [params, setParams] = useSearchParams();
  const esModerador = usuario.rol !== "ESTUDIANTE";
  const { datos: reportados, recargar: recargarReportados } = useAsync(
    () => (esModerador ? api.chat.reportados() : Promise.resolve([])),
    [esModerador],
  );
  const vista =
    esModerador && params.get("vista") === "reportes"
      ? "reportes"
      : "conversacion";
  const cambiarVista = (v) =>
    setParams(v === "reportes" ? { vista: "reportes" } : {}, { replace: true });

  if (cargando && !datos) return <Cargando texto="Cargando tus cursos…" />;
  if (error && !datos)
    return <EstadoError mensaje={error} onReintentar={recargar} />;
  const lista = (datos ?? []).filter(
    (c) => usuario.rol !== "ESTUDIANTE" || c.inscrito !== false,
  );
  const actual = lista.find((c) => String(c.id) === cursoId);

  return (
    <>
      <VolverACursos />
      <PageHeader
        eyebrow="Comunidad"
        titulo="Conversación del curso"
        subtitulo={
          usuario.rol === "ADMINISTRADOR"
            ? "Supervisa las conversaciones de todos los cursos. Revisa los mensajes reportados y decide si se eliminan o se mantienen."
            : usuario.rol === "INSTRUCTOR"
              ? "Acompaña a tus estudiantes y resuelve sus dudas. Revisa los mensajes reportados de tus cursos y decide si se eliminan o se mantienen."
              : "Resuelve dudas con tu grupo. Sé respetuoso: los mensajes se pueden reportar."
        }
      />
      {esModerador && (
        <Pestanas
          etiqueta="Secciones de la comunidad"
          activa={vista}
          onCambiar={cambiarVista}
          pestanas={[
            { id: "conversacion", texto: "Conversación" },
            {
              id: "reportes",
              texto: `Mensajes reportados${reportados?.length ? ` (${reportados.length})` : ""}`,
            },
          ]}
        />
      )}
      {vista === "reportes" ? (
        <div role="tabpanel" id="panel-reportes" aria-labelledby="tab-reportes">
          <Reportados onCambio={recargarReportados} />
        </div>
      ) : lista.length === 0 ? (
        <Vacio titulo="Todavía no tienes cursos con comunidad">
          {usuario.rol === "ESTUDIANTE" && (
            <p>
              Inscríbete en un curso desde{" "}
              <Link className="font-bold underline" to="/cursos">
                Mis cursos
              </Link>
              .
            </p>
          )}
        </Vacio>
      ) : (
        <div
          className="grid gap-4"
          id="panel-conversacion"
          role={esModerador ? "tabpanel" : undefined}
          aria-labelledby={esModerador ? "tab-conversacion" : undefined}
        >
          <SelectField
            label="Curso"
            className="max-w-md"
            value={cursoId ?? ""}
            onChange={(e) =>
              nav(
                e.target.value ? `/comunidad/${e.target.value}` : "/comunidad",
              )
            }
          >
            <option value="">Elige un curso…</option>
            {lista.map((c) => (
              <option key={c.id} value={c.id}>
                {c.titulo}
              </option>
            ))}
          </SelectField>
          {actual ? (
            <Sala
              key={actual.id}
              curso={actual.id}
              onModerado={recargarReportados}
            />
          ) : (
            <Banner tono="info">
              Elige un curso para ver su conversación.
            </Banner>
          )}
        </div>
      )}
    </>
  );
}

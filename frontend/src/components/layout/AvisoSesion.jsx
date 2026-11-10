import { useEffect, useId, useRef, useState } from "react";
import { api, mensajeError, token } from "../../api/client.js";
import { useAuth } from "../../context/AuthContext.jsx";
import { useNotif } from "../../context/NotifContext.jsx";
import { Banner, Button } from "../ui/index.jsx";

/** RF-042: el aviso aparece 2 minutos antes de que termine la sesión. */
const SEGUNDOS_AVISO = 120;

const hora = (ms) => new Date(ms).toLocaleTimeString("es-CO", { hour: "2-digit", minute: "2-digit" });
const fmtRestante = (s) => {
  const m = Math.floor(s / 60);
  const r = s % 60;
  return m > 0 ? `${m} min ${String(r).padStart(2, "0")} s` : `${r} s`;
};

/**
 * RF-042: aviso accesible antes de que termine la sesión. Cada respuesta del servidor trae en la
 * cabecera X-Sesion-Restante los segundos que quedan (inactividad o tope del token, lo que llegue
 * antes). A 2 minutos se abre un diálogo de alerta que el lector de pantalla anuncia y que se
 * opera con teclado; «Ampliar sesión» renueva el token. Si nadie responde, la sesión se cierra:
 * lo ya guardado (por ejemplo, las respuestas de una evaluación) se conserva en el servidor.
 */
export default function AvisoSesion() {
  const { usuario } = useAuth();
  const { avisar } = useNotif();
  const [fin, setFin] = useState(null);
  const [ahora, setAhora] = useState(() => Date.now());
  const [abierto, setAbierto] = useState(false);
  const [trabajando, setTrabajando] = useState(false);
  const [error, setError] = useState(null);
  const dialogo = useRef(null);
  const idTitulo = useId();
  const idTexto = useId();

  useEffect(() => {
    const alRecibir = (e) => {
      if (Number.isFinite(e.detail)) setFin(Date.now() + e.detail * 1000);
    };
    window.addEventListener("vl:sesion-restante", alRecibir);
    return () => window.removeEventListener("vl:sesion-restante", alRecibir);
  }, []);

  useEffect(() => {
    if (!usuario || fin === null) return undefined;
    const t = setInterval(() => setAhora(Date.now()), 1000);
    return () => clearInterval(t);
  }, [usuario, fin]);

  const restante = fin === null ? null : Math.max(0, Math.ceil((fin - ahora) / 1000));

  useEffect(() => {
    if (!usuario || restante === null) return;
    if (restante <= 0) {
      setAbierto(false);
      token.clear();
      window.dispatchEvent(new CustomEvent("vl:sesion-expirada", {
        detail: "Tu sesión terminó por tiempo. Lo que ya habías guardado se conservó. Inicia sesión de nuevo para continuar.",
      }));
      setFin(null);
      return;
    }
    if (restante <= SEGUNDOS_AVISO && !abierto) setAbierto(true);
    if (restante > SEGUNDOS_AVISO && abierto) setAbierto(false);
  }, [usuario, restante, abierto]);

  useEffect(() => {
    const d = dialogo.current;
    if (!d) return;
    if (abierto && !d.open) d.showModal();
    if (!abierto && d.open) d.close();
  }, [abierto]);

  if (!usuario) return null;

  const ampliar = async () => {
    setTrabajando(true);
    setError(null);
    try {
      const r = await api.auth.refresh();
      token.set(r.token);
      const nuevoFin = Date.now() + r.segundosRestantes * 1000;
      setFin(nuevoFin);
      setAhora(Date.now());
      setAbierto(false);
      avisar(`Ampliamos tu sesión. Ahora termina a las ${hora(nuevoFin)} si no hay actividad.`, "ok");
    } catch (e) {
      setError(mensajeError(e, "No pudimos ampliar tu sesión. Guarda lo que estés haciendo."));
    } finally {
      setTrabajando(false);
    }
  };

  return (
    <dialog ref={dialogo} role="alertdialog" aria-labelledby={idTitulo} aria-describedby={idTexto}
      onCancel={(e) => { e.preventDefault(); setAbierto(false); }}
      className="m-auto w-[calc(100vw-2rem)] max-w-md rounded-screen border border-line bg-surface p-0 backdrop:bg-black/50">
      {abierto && (
        <div className="grid gap-4 p-6">
          <h2 id={idTitulo}>Tu sesión está por terminar</h2>
          <p id={idTexto} className="text-body">
            Por seguridad, la sesión se cierra tras un tiempo sin actividad. Quedan{" "}
            <strong className="text-strong">{fmtRestante(restante ?? 0)}</strong>. ¿Quieres seguir conectado?
          </p>
          {error && <Banner tono="err">{error}</Banner>}
          <div className="flex flex-wrap gap-3">
            <Button onClick={ampliar} cargando={trabajando}>Ampliar sesión</Button>
            <Button variante="secondary" onClick={() => setAbierto(false)} disabled={trabajando}>Cerrar aviso</Button>
          </div>
        </div>
      )}
    </dialog>
  );
}

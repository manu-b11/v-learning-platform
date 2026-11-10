import { useEffect, useRef, useState } from "react";
import { Hand } from "lucide-react";
import { Button } from "./ui/index.jsx";
import { desincronizado, tiempoInterprete } from "../utils/senas.js";

/**
 * RF-040: video del intérprete de lengua de señas al lado del contenido. Con un video original
 * se reproduce sincronizado (play, pausa, saltos y velocidad) y se corrige si se separa más de
 * 250 ms; con una lectura se reproduce solo, con sus propios controles.
 */
export default function InterpreteSenas({ medio, url, desfaseMs = 0, abiertoAlInicio = false }) {
  const [abierto, setAbierto] = useState(abiertoAlInicio);
  const interprete = useRef(null);
  const conOriginal = Boolean(medio);

  useEffect(() => {
    const m = medio?.current;
    if (!abierto || !m) return undefined;
    const sincronizar = () => {
      const i = interprete.current;
      if (!i) return;
      if (desincronizado(i.currentTime, m.currentTime, desfaseMs)) i.currentTime = tiempoInterprete(m.currentTime, desfaseMs);
      if (i.playbackRate !== m.playbackRate) i.playbackRate = m.playbackRate;
    };
    const reproducir = () => { sincronizar(); interprete.current?.play().catch(() => {}); };
    const pausar = () => { interprete.current?.pause(); sincronizar(); };
    m.addEventListener("play", reproducir);
    m.addEventListener("pause", pausar);
    m.addEventListener("seeked", sincronizar);
    m.addEventListener("timeupdate", sincronizar);
    m.addEventListener("ratechange", sincronizar);
    sincronizar();
    return () => {
      m.removeEventListener("play", reproducir);
      m.removeEventListener("pause", pausar);
      m.removeEventListener("seeked", sincronizar);
      m.removeEventListener("timeupdate", sincronizar);
      m.removeEventListener("ratechange", sincronizar);
    };
  }, [abierto, medio, desfaseMs]);

  return (
    <div className="grid content-start gap-2">
      <Button variante="secondary" icono={Hand} aria-pressed={abierto} onClick={() => setAbierto((a) => !a)}>
        {abierto ? "Ocultar intérprete de lengua de señas" : "Mostrar intérprete de lengua de señas"}
      </Button>
      {abierto && (
        <video ref={interprete} src={url} muted playsInline controls={!conOriginal} preload="auto"
          aria-label="Intérprete de lengua de señas" className="w-full rounded-card bg-black">
          Tu navegador no puede reproducir el video del intérprete.
        </video>
      )}
      {abierto && conOriginal && (
        <p className="text-sm text-muted">El intérprete sigue al video principal: usa sus controles para reproducir o pausar.</p>
      )}
    </div>
  );
}

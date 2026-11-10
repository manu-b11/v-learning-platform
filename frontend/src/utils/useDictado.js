import { useCallback, useEffect, useRef, useState } from "react";

/** RF-045: constructor de reconocimiento de voz del navegador, o null si no lo admite. */
export function reconocimientoDisponible() {
  if (typeof window === "undefined") return null;
  return window.SpeechRecognition || window.webkitSpeechRecognition || null;
}

/** Une el texto que ya había con lo dictado, con un espacio entre medio. */
export function unirDictado(actual, dictado) {
  const nuevo = (dictado ?? "").trim();
  if (!nuevo) return actual ?? "";
  const base = actual ?? "";
  return base && !/\s$/.test(base) ? `${base} ${nuevo}` : `${base}${nuevo}`;
}

const CAUSAS = {
  "not-allowed": "No diste permiso para usar el micrófono. Puedes activarlo en la configuración del navegador.",
  "service-not-allowed": "Tu navegador no permite el dictado por voz en esta página.",
  "audio-capture": "No encontramos un micrófono conectado.",
  network: "El dictado necesita conexión a internet en este navegador.",
};

/**
 * RF-045: dictado por voz con la Web Speech API. Cada frase reconocida se agrega al campo
 * (onTexto) en cuanto termina; lo que se va reconociendo queda en `parcial`. Sin soporte del
 * navegador o sin permiso, `soportado` es false y `causa` explica por qué.
 */
export function useDictado(onTexto, { lang = "es-CO" } = {}) {
  const Reconocimiento = reconocimientoDisponible();
  const [escuchando, setEscuchando] = useState(false);
  const [parcial, setParcial] = useState("");
  const [causa, setCausa] = useState(Reconocimiento ? null : "Tu navegador no permite dictar por voz. Prueba con Chrome o Edge.");
  const rec = useRef(null);
  const alTexto = useRef(onTexto);

  useEffect(() => { alTexto.current = onTexto; }, [onTexto]);
  useEffect(() => () => rec.current?.abort(), []);

  const detener = useCallback(() => {
    rec.current?.stop();
  }, []);

  const iniciar = useCallback(() => {
    if (!Reconocimiento || rec.current) return;
    const r = new Reconocimiento();
    r.lang = lang;
    r.continuous = true;
    r.interimResults = true;
    r.onresult = (e) => {
      let provisional = "";
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const frase = e.results[i][0].transcript;
        if (e.results[i].isFinal) alTexto.current?.(frase);
        else provisional += frase;
      }
      setParcial(provisional);
    };
    r.onerror = (e) => {
      if (CAUSAS[e.error]) setCausa(CAUSAS[e.error]);
    };
    r.onend = () => {
      rec.current = null;
      setEscuchando(false);
      setParcial("");
    };
    rec.current = r;
    r.start();
    setEscuchando(true);
  }, [Reconocimiento, lang]);

  return { soportado: Boolean(Reconocimiento) && !causa, causa, escuchando, parcial, iniciar, detener };
}

import { useCallback, useEffect, useRef, useState } from "react";

/** Velocidades disponibles para la lectura en voz alta (RF-018). */
export const VELOCIDADES_VOZ = [0.75, 1, 1.25, 1.5, 2];

/**
 * Divide el texto en frases cortas. Leer por frases permite retroceder (la Web Speech API no tiene
 * posición ni "seek"), cambiar la velocidad sin empezar de cero y evita que el navegador corte
 * locuciones largas.
 */
export function dividirEnFrases(texto, max = 220) {
  const base = String(texto ?? "")
    .replace(/\s+/g, " ")
    .split(/(?<=[.!?…:;])\s+/)
    .map((f) => f.trim())
    .filter(Boolean);
  const frases = [];
  for (const f of base) {
    if (f.length <= max) {
      frases.push(f);
      continue;
    }
    let resto = f;
    while (resto.length > max) {
      let corte = resto.lastIndexOf(" ", max);
      if (corte < max / 2) corte = max;
      frases.push(resto.slice(0, corte).trim());
      resto = resto.slice(corte).trim();
    }
    if (resto) frases.push(resto);
  }
  return frases;
}

/**
 * Lectura en voz alta con controles (RF-018): reproducir, pausar/reanudar, retroceder una frase,
 * cambiar velocidad y detener. estado: "detenido" | "leyendo" | "pausado".
 */
export function useLecturaVoz(texto, { lang = "es-CO" } = {}) {
  const [estado, setEstado] = useState("detenido");
  const [indice, setIndice] = useState(0);
  const [velocidad, setVelocidad] = useState(1);
  const [total, setTotal] = useState(0);
  const frases = useRef([]);
  const turno = useRef(0); // invalida los onend de locuciones canceladas
  const siguiente = useRef(null); // referencia a hablar() para encadenar la frase siguiente
  const soportado = typeof window !== "undefined" && "speechSynthesis" in window;

  const hablar = useCallback(
    (i, rate) => {
      const s = window.speechSynthesis;
      const lista = frases.current;
      if (!s || i >= lista.length) {
        setEstado("detenido");
        setIndice(0);
        return;
      }
      const miTurno = ++turno.current;
      s.cancel();
      s.resume(); // si estaba en pausa, la nueva frase debe sonar
      const u = new SpeechSynthesisUtterance(lista[i]);
      u.lang = lang;
      u.rate = rate;
      u.onend = () => {
        if (miTurno !== turno.current) return;
        if (i + 1 < lista.length) siguiente.current?.(i + 1, rate);
        else {
          setEstado("detenido");
          setIndice(0);
        }
      };
      u.onerror = (e) => {
        if (miTurno !== turno.current || e.error === "interrupted" || e.error === "canceled") return;
        setEstado("detenido");
      };
      setIndice(i);
      setEstado("leyendo");
      s.speak(u);
    },
    [lang],
  );

  useEffect(() => {
    siguiente.current = hablar;
  }, [hablar]);

  const iniciar = useCallback(() => {
    frases.current = dividirEnFrases(texto);
    setTotal(frases.current.length);
    if (!frases.current.length) return false;
    hablar(0, velocidad);
    return true;
  }, [texto, velocidad, hablar]);

  const pausar = useCallback(() => {
    window.speechSynthesis?.pause();
    setEstado("pausado");
  }, []);

  const reanudar = useCallback(() => {
    window.speechSynthesis?.resume();
    setEstado("leyendo");
  }, []);

  const retroceder = useCallback(() => {
    if (estado === "detenido") return;
    hablar(Math.max(0, indice - 1), velocidad);
  }, [estado, indice, velocidad, hablar]);

  const cambiarVelocidad = useCallback(
    (v) => {
      setVelocidad(v);
      if (estado !== "detenido") hablar(indice, v); // repite la frase actual con la nueva velocidad
    },
    [estado, indice, hablar],
  );

  const detener = useCallback(() => {
    turno.current++;
    window.speechSynthesis?.cancel();
    setEstado("detenido");
    setIndice(0);
  }, []);

  // Al salir de la pantalla o cambiar de texto, se detiene la lectura.
  useEffect(() => () => {
    turno.current++;
    window.speechSynthesis?.cancel();
  }, [texto]);

  return {
    soportado,
    estado,
    indice,
    total,
    velocidad,
    iniciar,
    pausar,
    reanudar,
    retroceder,
    cambiarVelocidad,
    detener,
  };
}

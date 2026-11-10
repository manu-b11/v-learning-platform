/** RF-040: reglas de sincronización del video de lengua de señas con el original. */

/** Diferencia máxima tolerada antes de corregir (el requisito pide menos de 300 ms). */
export const UMBRAL_SINCRONIA = 0.25;

/** Momento del intérprete que corresponde a un momento del original. */
export function tiempoInterprete(tiempoOriginal, desfaseMs = 0) {
  return Math.max(0, tiempoOriginal + (Number(desfaseMs) || 0) / 1000);
}

/** true si el intérprete se separó del original más del umbral. */
export function desincronizado(tiempoInterpreteActual, tiempoOriginal, desfaseMs = 0, umbral = UMBRAL_SINCRONIA) {
  return Math.abs(tiempoInterpreteActual - tiempoInterprete(tiempoOriginal, desfaseMs)) > umbral;
}

/** Aviso para el instructor si las duraciones no coinciden (más de 2 s de diferencia). */
export function avisoDuraciones(duracionOriginal, duracionInterprete) {
  if (!Number.isFinite(duracionOriginal) || !Number.isFinite(duracionInterprete)) return null;
  const dif = duracionInterprete - duracionOriginal;
  if (Math.abs(dif) <= 2) return null;
  const s = Math.round(Math.abs(dif));
  return `El video del intérprete dura ${s} s ${dif > 0 ? "más" : "menos"} que el original. Ajusta el desfase para que vayan a la par.`;
}

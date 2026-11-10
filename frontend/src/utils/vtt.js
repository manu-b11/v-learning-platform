/**
 * RF-038: lectura de subtítulos WebVTT y conversión a texto plano y SRT. La transcripción
 * sincronizada usa el mismo archivo VTT de los subtítulos aprobados.
 */

const TIEMPO = /^(?:(\d+):)?(\d{1,2}):(\d{2})[.,](\d{1,3})$/;

/** "01:02:03.456" o "02:03.456" → segundos. */
export function aSegundos(marca) {
  const m = TIEMPO.exec(marca.trim());
  if (!m) return null;
  const [, h, min, s, ms] = m;
  return Number(h ?? 0) * 3600 + Number(min) * 60 + Number(s) + Number(ms.padEnd(3, "0")) / 1000;
}

/** Segundos → "00:01:02,500" (SRT) o "01:02" (etiqueta visible). */
export function formatoSrt(seg) {
  const total = Math.max(0, Math.round(seg * 1000));
  const h = Math.floor(total / 3600000);
  const m = Math.floor((total % 3600000) / 60000);
  const s = Math.floor((total % 60000) / 1000);
  const ms = total % 1000;
  const p = (n, l = 2) => String(n).padStart(l, "0");
  return `${p(h)}:${p(m)}:${p(s)},${p(ms, 3)}`;
}

export function etiquetaTiempo(seg) {
  const t = Math.floor(seg);
  const h = Math.floor(t / 3600);
  const m = Math.floor((t % 3600) / 60);
  const s = t % 60;
  const p = (n) => String(n).padStart(2, "0");
  return h > 0 ? `${h}:${p(m)}:${p(s)}` : `${p(m)}:${p(s)}`;
}

/** Texto VTT → [{ inicio, fin, texto }] en orden. Ignora encabezado, notas y estilos. */
export function parseVtt(texto) {
  const bloques = String(texto ?? "").replace(/\r\n?/g, "\n").split(/\n{2,}/);
  const lineas = [];
  for (const bloque of bloques) {
    const filas = bloque.split("\n").filter((f) => f.trim() !== "");
    const i = filas.findIndex((f) => f.includes("-->"));
    if (i < 0) continue;
    const [a, b] = filas[i].split("-->");
    const inicio = aSegundos(a);
    const fin = aSegundos(b.trim().split(/\s+/)[0]);
    if (inicio === null || fin === null) continue;
    const contenido = filas.slice(i + 1).join(" ").replace(/<[^>]+>/g, "").replace(/\s+/g, " ").trim();
    if (contenido) lineas.push({ inicio, fin, texto: contenido });
  }
  return lineas.sort((x, y) => x.inicio - y.inicio);
}

/** Índice de la línea que se está reproduciendo (o -1). */
export function lineaActual(lineas, tiempo) {
  let actual = -1;
  for (let i = 0; i < lineas.length; i++) {
    if (lineas[i].inicio <= tiempo + 0.05) actual = i;
    else break;
  }
  return actual;
}

export function aSrt(lineas) {
  return lineas.map((l, i) => `${i + 1}\n${formatoSrt(l.inicio)} --> ${formatoSrt(l.fin)}\n${l.texto}\n`).join("\n");
}

export function aTxt(lineas) {
  return lineas.map((l) => `[${etiquetaTiempo(l.inicio)}] ${l.texto}`).join("\n") + "\n";
}

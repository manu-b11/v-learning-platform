/** RF-041: texto corto de una acomodación, para no depender del color en la tabla de notas. */
export function resumenAcomodacion(a) {
  if (!a) return null;
  const partes = [];
  if (a.tiempoIlimitado) partes.push("Tiempo ilimitado");
  else if (a.multiplicadorTiempo) partes.push(`${String(Number(a.multiplicadorTiempo)).replace(".", ",")}x de tiempo`);
  if (a.minutosAmpliacion > 0) partes.push(`+${a.minutosAmpliacion} min a pedido`);
  if (a.intentosExtra > 0) partes.push(`${a.intentosExtra} intento(s) extra`);
  if (a.fechaLimiteEspecial) partes.push("fecha especial");
  return partes.join(" · ") + (a.vencida ? " (vencida)" : "");
}

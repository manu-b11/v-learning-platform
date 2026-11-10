import { Mic, MicOff } from "lucide-react";
import { unirDictado, useDictado } from "../utils/useDictado.js";
import { cx } from "../utils/cx.js";

/**
 * RF-045: botón de micrófono para dictar en un campo de texto. Agrega cada frase al valor actual,
 * que se puede corregir antes de enviar. Si el navegador no lo admite o se niega el permiso, el
 * botón no se muestra y se explica la causa.
 */
export default function BotonDictado({ valor, onCambio, campo, className }) {
  const dictado = useDictado((frase) => onCambio(unirDictado(valor, frase)));

  if (!dictado.soportado) {
    return <p className={cx("mt-1 text-sm text-muted", className)}>{dictado.causa}</p>;
  }
  const Icono = dictado.escuchando ? MicOff : Mic;
  return (
    <div className={cx("mt-2 flex flex-wrap items-center gap-3", className)}>
      <button type="button" aria-pressed={dictado.escuchando}
        onClick={dictado.escuchando ? dictado.detener : dictado.iniciar}
        className={cx("vl-btn inline-flex min-h-11 items-center gap-2 rounded-ctl border-[1.5px] px-4 font-bold",
          dictado.escuchando ? "border-err-fg bg-err-bg text-err-fg" : "border-line bg-surface text-strong hover:bg-subtle")}>
        <Icono size={18} aria-hidden="true" />
        {dictado.escuchando ? "Detener dictado" : "Dictar"}
        <span className="sr-only"> {campo}</span>
      </button>
      <p role="status" aria-live="polite" className="text-sm text-muted">
        {dictado.escuchando ? `Escuchando… ${dictado.parcial}` : ""}
      </p>
    </div>
  );
}

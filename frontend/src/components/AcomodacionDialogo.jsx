import { useState } from "react";
import { api, mensajeError } from "../api/client.js";
import { useNotif } from "../context/NotifContext.jsx";
import { Banner, Button, Dialogo, SelectField, TextField, Toggle } from "./ui/index.jsx";

/** "2026-11-30T23:59:00" → "2026-11-30T23:59" (formato de <input type="datetime-local">). */
const aLocal = (v) => (v ? String(v).slice(0, 16) : "");

/**
 * RF-041: asignar o retirar las acomodaciones de evaluación de un estudiante. Se aplican a todas
 * las evaluaciones del curso; el servidor calcula el tiempo y aplica el mayor entre la acomodación
 * y el tiempo adicional del perfil del estudiante.
 */
export default function AcomodacionDialogo({ cursoId, estudiante, actual, onCerrar, onGuardado }) {
  const { avisar } = useNotif();
  const [multiplicador, setMultiplicador] = useState(actual?.multiplicadorTiempo ? String(Number(actual.multiplicadorTiempo)) : "");
  const [ilimitado, setIlimitado] = useState(actual?.tiempoIlimitado ?? false);
  const [intentos, setIntentos] = useState(String(actual?.intentosExtra ?? 0));
  const [ampliacion, setAmpliacion] = useState(String(actual?.minutosAmpliacion ?? 0));
  const [fechaEspecial, setFechaEspecial] = useState(aLocal(actual?.fechaLimiteEspecial));
  const [vigencia, setVigencia] = useState(actual?.vigenteHasta ?? "");
  const [error, setError] = useState(null);
  const [trabajando, setTrabajando] = useState(false);

  const guardar = async (e) => {
    e.preventDefault();
    setError(null);
    setTrabajando(true);
    try {
      await api.acomodaciones.asignar(cursoId, {
        estudianteId: estudiante.estudianteId,
        multiplicadorTiempo: ilimitado || !multiplicador ? null : Number(multiplicador),
        tiempoIlimitado: ilimitado,
        intentosExtra: Number(intentos) || 0,
        minutosAmpliacion: Number(ampliacion) || 0,
        fechaLimiteEspecial: fechaEspecial ? `${fechaEspecial}:00` : null,
        vigenteHasta: vigencia || null,
      });
      avisar(`Acomodación guardada para ${estudiante.nombre}.`, "ok");
      onGuardado();
    } catch (err) {
      setError(mensajeError(err, "No pudimos guardar la acomodación."));
    } finally {
      setTrabajando(false);
    }
  };

  const retirar = async () => {
    setError(null);
    setTrabajando(true);
    try {
      await api.acomodaciones.quitar(cursoId, actual.id);
      avisar(`Se retiró la acomodación de ${estudiante.nombre}.`, "ok");
      onGuardado();
    } catch (err) {
      setError(mensajeError(err, "No pudimos retirar la acomodación."));
    } finally {
      setTrabajando(false);
    }
  };

  return (
    <Dialogo abierto onCerrar={onCerrar} titulo={`Acomodaciones de ${estudiante.nombre}`} ancho="max-w-xl">
      <form onSubmit={guardar} className="grid gap-4" noValidate>
        <p className="text-body">
          Se aplican a todas las evaluaciones del curso, también a las que crees después. Si el estudiante ya tiene
          tiempo adicional en su perfil, se usa el mayor de los dos (no se suman).
        </p>
        {actual?.vencida && <Banner tono="warn">Esta acomodación venció y no se está aplicando. Cambia la vigencia para renovarla.</Banner>}
        <Toggle label="Tiempo ilimitado" descripcion="Las evaluaciones no tendrán cronómetro para este estudiante."
          checked={ilimitado} onChange={setIlimitado} />
        <SelectField label="Multiplicador de tiempo" value={multiplicador} disabled={ilimitado}
          onChange={(e) => setMultiplicador(e.target.value)}>
          <option value="">Sin multiplicador</option>
          <option value="1.5">1,5x (50 % más de tiempo)</option>
          <option value="2">2x (el doble de tiempo)</option>
        </SelectField>
        <TextField label="Minutos que puede pedir al recibir el aviso de tiempo" type="number" min={0} max={60}
          inputMode="numeric" value={ampliacion} onChange={(e) => setAmpliacion(e.target.value)}
          ayuda="Cuando le queden 2 minutos podrá ampliar el intento una vez con estos minutos. 0 = no puede ampliar." />
        <TextField label="Intentos extra" type="number" min={0} max={5} inputMode="numeric" value={intentos}
          onChange={(e) => setIntentos(e.target.value)}
          ayuda="Hoy las evaluaciones no limitan los intentos, así que este valor se guarda pero aún no cambia nada." />
        <TextField label="Fecha límite especial para iniciar (opcional)" type="datetime-local" value={fechaEspecial}
          onChange={(e) => setFechaEspecial(e.target.value)}
          ayuda="Solo se usa si es posterior a la fecha límite de cada evaluación." />
        <TextField label="Vigente hasta (opcional)" type="date" value={vigencia} onChange={(e) => setVigencia(e.target.value)}
          ayuda="Último día en que se aplica. Déjala vacía si no vence." />
        {error && <Banner tono="err">{error}</Banner>}
        <div className="flex flex-wrap gap-3">
          <Button type="submit" cargando={trabajando}>Guardar acomodación</Button>
          {actual && <Button variante="secondary" onClick={retirar} disabled={trabajando}>Retirar acomodación</Button>}
          <Button variante="link" onClick={onCerrar} disabled={trabajando}>Cancelar</Button>
        </div>
      </form>
    </Dialogo>
  );
}

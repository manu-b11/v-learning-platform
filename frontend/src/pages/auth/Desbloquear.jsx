import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api, codigoError, mensajeError } from "../../api/client.js";
import { AuthShell } from "../../components/layout/Shells.jsx";
import { Banner, Button, TextField } from "../../components/ui/index.jsx";

/**
 * RF-043: desbloquear la cuenta con el enlace del correo. El enlace no se canjea al abrir la página
 * (los antivirus del correo abren enlaces solos), sino al pulsar el botón. Si venció, se pide otro.
 */
export default function Desbloquear() {
  const [params] = useSearchParams();
  const token = params.get("token");
  const [hecho, setHecho] = useState(null);
  const [error, setError] = useState(null);
  const [pedirOtro, setPedirOtro] = useState(!token);
  const [correo, setCorreo] = useState("");
  const [respuesta, setRespuesta] = useState(null);
  const [cargando, setCargando] = useState(false);

  const desbloquear = async () => {
    setError(null);
    setCargando(true);
    try {
      setHecho((await api.auth.desbloquear(token)).mensaje);
    } catch (err) {
      setError(mensajeError(err, "No pudimos desbloquear tu cuenta."));
      if (codigoError(err) === "ENLACE_INVALIDO") setPedirOtro(true);
    } finally {
      setCargando(false);
    }
  };

  const solicitar = async (e) => {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      setRespuesta(await api.auth.solicitarDesbloqueo(correo.trim()));
    } catch (err) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  };

  return (
    <AuthShell>
      <h1 className="text-3xl">Desbloquea tu cuenta</h1>
      <p className="mt-1.5 text-lg">
        Tu cuenta se bloqueó por varios intentos fallidos. Con el enlace que te enviamos la recuperas sin esperar.
      </p>
      {error && <Banner tono="err" className="mt-5">{error}</Banner>}

      {hecho ? (
        <div className="mt-6 grid gap-4">
          <Banner tono="ok" rol="status">{hecho}</Banner>
          <Button to="/login" bloque>Iniciar sesión</Button>
        </div>
      ) : (
        <>
          {token && !pedirOtro && (
            <div className="mt-6 grid gap-4">
              <p>El enlace es de un solo uso y vence a los 15 minutos.</p>
              <Button bloque cargando={cargando} onClick={desbloquear}>Desbloquear mi cuenta</Button>
            </div>
          )}
          {pedirOtro && (
            respuesta ? (
              <div className="mt-6 grid gap-4">
                <Banner tono="ok" rol="status">{respuesta.mensaje}</Banner>
                {respuesta.enlaceDesbloqueo && (
                  <Banner tono="warn"><strong>Modo desarrollo:</strong> abre este enlace para continuar:{" "}
                    <a className="break-all underline" href={respuesta.enlaceDesbloqueo}>{respuesta.enlaceDesbloqueo}</a></Banner>
                )}
              </div>
            ) : (
              <form onSubmit={solicitar} className="mt-6 grid gap-5" noValidate>
                <h2 className="text-xl">Pide un enlace nuevo</h2>
                <TextField label="Correo institucional" type="email" autoComplete="username" required
                  value={correo} onChange={(e) => setCorreo(e.target.value)} />
                <Button type="submit" bloque cargando={cargando} disabled={!correo.trim()}
                  motivo={!correo.trim() ? "Escribe tu correo para continuar." : undefined}>Enviar enlace</Button>
              </form>
            )
          )}
          <p className="mt-5 text-center">
            <Link to="/login" className="font-bold text-primary-600 underline">Volver al inicio de sesión</Link>
          </p>
        </>
      )}
    </AuthShell>
  );
}

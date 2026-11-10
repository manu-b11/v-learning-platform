import { useEffect, useRef, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { api, codigoError, mensajeError } from "../../api/client.js";
import { useAuth } from "../../context/AuthContext.jsx";
import { destinoInicial } from "../../utils/destino.js";
import { AuthShell } from "../../components/layout/Shells.jsx";
import {
  Banner,
  Button,
  PasswordField,
  TextField,
} from "../../components/ui/index.jsx";

/** RF-002: credenciales → código OTP (omitido si el estudiante lo desactivó) → sesión. Redirige según rol y lo que falte por completar. */
export default function Login() {
  const { usuario, verificarOtp, iniciarSesion, avisoSesion } = useAuth();
  const navigate = useNavigate();
  const [paso, setPaso] = useState(1);
  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [codigo, setCodigo] = useState("");
  const [otpDev, setOtpDev] = useState(null);
  const [error, setError] = useState(null);
  const [info, setInfo] = useState(null);
  const [bloqueada, setBloqueada] = useState(false);
  const [cargando, setCargando] = useState(false);
  // RF-044: error asociado al campo del código, reenvío con espera de 60 s y foco en el campo.
  const [errorCodigo, setErrorCodigo] = useState(null);
  const [espera, setEspera] = useState(0);
  const [reenviando, setReenviando] = useState(false);
  const campoCodigo = useRef(null);

  useEffect(() => {
    if (espera <= 0) return undefined;
    const t = setTimeout(() => setEspera((s) => s - 1), 1000);
    return () => clearTimeout(t);
  }, [espera]);

  if (usuario) return <Navigate to={destinoInicial(usuario)} replace />;

  const enviarCredenciales = async (e) => {
    e.preventDefault();
    setError(null);
    setBloqueada(false);
    setCargando(true);
    try {
      const r = await api.auth.login(correo.trim(), password);
      if (r.sesion) {
        // Estudiante con la verificación en dos pasos desactivada: entra sin código.
        const u = iniciarSesion(r.sesion);
        navigate(destinoInicial(u), { replace: true });
        return;
      }
      setOtpDev(r.codigoOtp ?? null);
      setInfo(r.mensaje);
      setErrorCodigo(null);
      setEspera(60);
      setPaso(2);
    } catch (err) {
      setError(
        mensajeError(
          err,
          "No pudimos iniciar sesión. Revisa tu correo y contraseña.",
        ),
      );
      // RF-043: con bloqueo temporal se ofrece el enlace de desbloqueo.
      setBloqueada(codigoError(err) === "CUENTA_BLOQUEADA");
    } finally {
      setCargando(false);
    }
  };

  const enviarCodigo = async (e) => {
    e.preventDefault();
    setError(null);
    if (codigo.length !== 6) {
      setErrorCodigo("Escribe los 6 dígitos del código.");
      campoCodigo.current?.focus();
      return;
    }
    setErrorCodigo(null);
    setCargando(true);
    try {
      const u = await verificarOtp(correo.trim(), codigo.trim());
      navigate(destinoInicial(u), { replace: true });
    } catch (err) {
      setErrorCodigo(
        mensajeError(
          err,
          "El código no es válido o venció. Revísalo o pide uno nuevo con «Reenviar código».",
        ),
      );
      campoCodigo.current?.focus();
    } finally {
      setCargando(false);
    }
  };

  const reenviar = async () => {
    setReenviando(true);
    setErrorCodigo(null);
    try {
      const r = await api.auth.reenviarOtp(correo.trim());
      setOtpDev(r.codigoOtp ?? null);
      setInfo(r.mensaje);
      setCodigo("");
      setEspera(60);
    } catch (err) {
      setErrorCodigo(mensajeError(err, "No pudimos enviar otro código. Inténtalo de nuevo en un momento."));
    } finally {
      setReenviando(false);
      campoCodigo.current?.focus();
    }
  };

  return (
    <AuthShell>
      <h1 className="text-3xl">
        {paso === 1 ? "Inicia sesión" : "Revisa tu código"}
      </h1>
      <p className="mt-1.5 text-lg">
        {paso === 1
          ? "Entra con tu correo institucional para seguir aprendiendo a tu manera."
          : "Te enviamos un código de 6 dígitos. Escríbelo para entrar."}
      </p>

      {avisoSesion && paso === 1 && (
        <Banner tono="warn" className="mt-5">
          {avisoSesion}
        </Banner>
      )}
      {error && (
        <Banner tono="err" className="mt-5">
          {error}
        </Banner>
      )}
      {bloqueada && paso === 1 && (
        <p className="mt-3">
          <Link to="/desbloquear" className="font-bold text-primary-600 underline">
            ¿No te llegó el enlace de desbloqueo? Pide otro
          </Link>
        </p>
      )}

      {paso === 1 ? (
        <form
          onSubmit={enviarCredenciales}
          className="mt-6 grid gap-5"
          noValidate
        >
          <TextField
            label="Correo institucional"
            type="email"
            autoComplete="username"
            required
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            placeholder="nombre@tdea.edu.co"
          />
          <PasswordField
            label="Contraseña"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <Button
            type="submit"
            bloque
            cargando={cargando}
            disabled={!correo.trim() || !password}
            motivo={
              !correo.trim() || !password
                ? "Escribe tu correo y tu contraseña para continuar."
                : undefined
            }
          >
            Continuar
          </Button>
          <p className="text-center">
            <Link
              to="/recuperar"
              className="font-bold text-primary-600 underline"
            >
              ¿Olvidaste tu contraseña?
            </Link>
          </p>
        </form>
      ) : (
        <form onSubmit={enviarCodigo} className="mt-6 grid gap-5" noValidate>
          {info && <Banner tono="info" rol="status">{info}</Banner>}
          {otpDev && (
            <Banner tono="warn">
              Tu código es{" "}
              <span className="font-mono text-lg font-extrabold">{otpDev}</span>
            </Banner>
          )}
          <TextField
            ref={campoCodigo}
            label="Código de 6 dígitos"
            ayuda="Puedes pegarlo desde tu correo. Si tiene espacios o guiones, los quitamos."
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={12}
            required
            value={codigo}
            error={errorCodigo}
            onChange={(e) => {
              setCodigo(e.target.value.replace(/\D/g, "").slice(0, 6));
              if (errorCodigo) setErrorCodigo(null);
            }}
            className="max-w-xs"
          />
          <div className="flex flex-wrap gap-3">
            <Button type="submit" cargando={cargando}>
              Entrar
            </Button>
            <Button
              variante="secondary"
              onClick={reenviar}
              cargando={reenviando}
              disabled={espera > 0}
              motivo={espera > 0 ? `Podrás pedir otro código en ${espera} segundos.` : undefined}
            >
              Reenviar código
            </Button>
            <Button
              variante="link"
              onClick={() => {
                setPaso(1);
                setCodigo("");
                setError(null);
                setErrorCodigo(null);
              }}
            >
              Volver
            </Button>
          </div>
        </form>
      )}
    </AuthShell>
  );
}

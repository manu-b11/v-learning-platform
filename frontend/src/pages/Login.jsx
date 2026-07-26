import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { login } from "../api/authService";

function Login() {
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    try {
      const data = await login(form);
      localStorage.setItem("token", data.token);
      navigate("/dashboard");
    } catch (err) {
      const message =
        err.response?.status === 401
          ? "Correo o contraseña incorrectos."
          : err.response?.data?.message ||
            "No se pudo iniciar sesión. Intenta de nuevo.";
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <span className="font-heading text-[22px] font-bold text-navy">
            V Learning
          </span>
        </div>

        <div className="card">
          <h1 className="text-[22px] tracking-tight">Iniciar sesión</h1>
          <p className="mt-1 text-sm text-text-secondary">
            Ingresa tus credenciales para continuar tu capacitación.
          </p>

          <form onSubmit={handleSubmit} className="mt-6 space-y-4">
            <label className="block">
              <span className="text-sm text-text-secondary">
                Correo electrónico
              </span>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                required
                autoFocus
                className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
              />
            </label>

            <label className="block">
              <div className="flex items-center justify-between">
                <span className="text-sm text-text-secondary">Contraseña</span>
                <Link
                  to="/forgot-password"
                  className="text-xs font-medium text-primary"
                >
                  ¿Olvidaste tu contraseña?
                </Link>
              </div>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                required
                className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
              />
            </label>

            {error && <p className="text-sm text-danger">{error}</p>}

            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary w-full disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isSubmitting ? "Ingresando..." : "Iniciar sesión"}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-text-secondary">
            ¿No tienes una cuenta?{" "}
            <Link to="/register" className="font-medium text-primary">
              Regístrate
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;

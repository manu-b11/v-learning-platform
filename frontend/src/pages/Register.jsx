import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { register } from "../api/authService";

function Register() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
  });
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
      const data = await register(form);
      // Ajusta esta línea al campo real que devuelva tu AuthResponse (token, jwt, etc.)
      localStorage.setItem("token", data.token);
      navigate("/dashboard");
    } catch (err) {
      const message =
        err.response?.data?.message ||
        "No se pudo completar el registro. Intenta de nuevo.";
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
          <h1 className="text-[22px] tracking-tight">Crear cuenta</h1>
          <p className="mt-1 text-sm text-text-secondary">
            Regístrate para comenzar tu capacitación.
          </p>

          <form onSubmit={handleSubmit} className="mt-6 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <label className="block">
                <span className="text-sm text-text-secondary">Nombre</span>
                <input
                  name="firstName"
                  value={form.firstName}
                  onChange={handleChange}
                  required
                  className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </label>

              <label className="block">
                <span className="text-sm text-text-secondary">Apellido</span>
                <input
                  name="lastName"
                  value={form.lastName}
                  onChange={handleChange}
                  required
                  className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </label>
            </div>

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
                className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
              />
            </label>

            <label className="block">
              <span className="text-sm text-text-secondary">Contraseña</span>
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                required
                minLength={8}
                className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
              />
            </label>

            {error && <p className="text-sm text-danger">{error}</p>}

            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary w-full disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isSubmitting ? "Creando cuenta..." : "Crear cuenta"}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-text-secondary">
            ¿Ya tienes una cuenta?{" "}
            <Link to="/login" className="font-medium text-primary">
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Register;

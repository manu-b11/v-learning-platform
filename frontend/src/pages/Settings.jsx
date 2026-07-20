import { useState } from "react";

import Layout from "../components/Layout";
import Toggle from "../components/Toggle";

function Settings() {
  const [passwords, setPasswords] = useState({
    current: "",
    new: "",
    confirm: "",
  });
  const [passwordError, setPasswordError] = useState("");

  const [notifications, setNotifications] = useState({
    email: true,
    evaluationReminders: true,
    newCourses: false,
    weeklyDigest: true,
  });

  const [preferences, setPreferences] = useState({
    learningStyle: "Visual",
    language: "es",
  });

  function handlePasswordChange(e) {
    const { name, value } = e.target;
    setPasswords((prev) => ({ ...prev, [name]: value }));
  }

  function handlePasswordSubmit() {
    if (passwords.new.length < 8) {
      setPasswordError("La nueva contraseña debe tener al menos 8 caracteres.");
      return;
    }
    if (passwords.new !== passwords.confirm) {
      setPasswordError("Las contraseñas no coinciden.");
      return;
    }
    setPasswordError("");
    // Aquí iría la llamada real a la API (p. ej. PUT /api/v1/users/me/password)
    setPasswords({ current: "", new: "", confirm: "" });
  }

  function toggleNotification(key) {
    setNotifications((prev) => ({ ...prev, [key]: !prev[key] }));
  }

  return (
    <Layout>
      <section>
        <h1 className="tracking-tight">Configuración</h1>
        <p className="mt-2 text-text-secondary">
          Administra tu contraseña, notificaciones y preferencias.
        </p>
      </section>

      {/* Cambiar contraseña */}
      <section className="card mt-8">
        <h2 className="tracking-tight">Cambiar contraseña</h2>

        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="block sm:col-span-2">
            <span className="text-sm text-text-secondary">
              Contraseña actual
            </span>
            <input
              type="password"
              name="current"
              value={passwords.current}
              onChange={handlePasswordChange}
              className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
            />
          </label>

          <label className="block">
            <span className="text-sm text-text-secondary">
              Nueva contraseña
            </span>
            <input
              type="password"
              name="new"
              value={passwords.new}
              onChange={handlePasswordChange}
              className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
            />
          </label>

          <label className="block">
            <span className="text-sm text-text-secondary">
              Confirmar nueva contraseña
            </span>
            <input
              type="password"
              name="confirm"
              value={passwords.confirm}
              onChange={handlePasswordChange}
              className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
            />
          </label>
        </div>

        {passwordError && (
          <p className="mt-3 text-sm text-danger">{passwordError}</p>
        )}

        <div className="mt-6 flex justify-end">
          <button onClick={handlePasswordSubmit} className="btn-primary">
            Actualizar contraseña
          </button>
        </div>
      </section>

      {/* Notificaciones */}
      <section className="card mt-8">
        <h2 className="tracking-tight">Notificaciones</h2>

        <div className="mt-4 divide-y divide-border">
          <div className="flex items-center justify-between py-4">
            <div>
              <p className="text-sm font-medium text-navy">
                Notificaciones por correo
              </p>
              <p className="mt-0.5 text-sm text-text-secondary">
                Recibe novedades importantes en tu bandeja de entrada.
              </p>
            </div>
            <Toggle
              checked={notifications.email}
              onChange={() => toggleNotification("email")}
            />
          </div>

          <div className="flex items-center justify-between py-4">
            <div>
              <p className="text-sm font-medium text-navy">
                Recordatorios de evaluaciones
              </p>
              <p className="mt-0.5 text-sm text-text-secondary">
                Avisos antes de que venza una evaluación pendiente.
              </p>
            </div>
            <Toggle
              checked={notifications.evaluationReminders}
              onChange={() => toggleNotification("evaluationReminders")}
            />
          </div>

          <div className="flex items-center justify-between py-4">
            <div>
              <p className="text-sm font-medium text-navy">
                Nuevos cursos disponibles
              </p>
              <p className="mt-0.5 text-sm text-text-secondary">
                Te avisamos cuando se publique un curso nuevo.
              </p>
            </div>
            <Toggle
              checked={notifications.newCourses}
              onChange={() => toggleNotification("newCourses")}
            />
          </div>

          <div className="flex items-center justify-between py-4">
            <div>
              <p className="text-sm font-medium text-navy">Resumen semanal</p>
              <p className="mt-0.5 text-sm text-text-secondary">
                Un correo semanal con tu progreso general.
              </p>
            </div>
            <Toggle
              checked={notifications.weeklyDigest}
              onChange={() => toggleNotification("weeklyDigest")}
            />
          </div>
        </div>
      </section>

      {/* Preferencias */}
      <section className="card mt-8">
        <h2 className="tracking-tight">Preferencias</h2>

        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="text-sm text-text-secondary">
              Estilo de aprendizaje preferido
            </span>
            <select
              value={preferences.learningStyle}
              onChange={(e) =>
                setPreferences((prev) => ({
                  ...prev,
                  learningStyle: e.target.value,
                }))
              }
              className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
            >
              <option value="Visual">Visual</option>
              <option value="Auditivo">Auditivo</option>
              <option value="Lectura/Escritura">Lectura/Escritura</option>
              <option value="Kinestésico">Kinestésico</option>
            </select>
          </label>

          <label className="block">
            <span className="text-sm text-text-secondary">Idioma</span>
            <select
              value={preferences.language}
              disabled
              className="mt-1 w-full cursor-not-allowed rounded-lg border border-border bg-background px-4 py-2 text-sm text-text-secondary"
            >
              <option value="es">Español</option>
            </select>
          </label>
        </div>

        <p className="mt-3 text-sm text-text-secondary">
          Este estilo se usa como respaldo si el diagnóstico inicial no está
          disponible; el contenido igual se sigue adaptando según tu desempeño
          real.
        </p>

        <div className="mt-6 flex justify-end">
          <button className="btn-primary">Guardar preferencias</button>
        </div>
      </section>
    </Layout>
  );
}

export default Settings;

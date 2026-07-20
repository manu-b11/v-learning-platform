import { useState } from "react";
import { Pencil } from "lucide-react";

import Layout from "../components/Layout";
import { varkSolidClass } from "../utils/vark";

const varkProfile = [
  { style: "Visual", percentage: 42 },
  { style: "Auditivo", percentage: 25 },
  { style: "Lectura/Escritura", percentage: 18 },
  { style: "Kinestésico", percentage: 15 },
];

function Profile() {
  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState({
    name: "Sara Gómez",
    email: "sara.gomez@laproff.com",
  });

  const dominant = varkProfile.reduce(
    (max, item) => (item.percentage > max.percentage ? item : max),
    varkProfile[0],
  );

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function handleSave() {
    // Aquí iría la llamada real a la API (p. ej. PUT /api/v1/users/me)
    setIsEditing(false);
  }

  return (
    <Layout>
      <section>
        <h1 className="tracking-tight">Perfil</h1>
        <p className="mt-2 text-text-secondary">
          Gestiona tu información personal y consulta tu perfil de aprendizaje.
        </p>
      </section>

      {/* Datos personales */}
      <section className="card mt-8">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-navy text-lg font-medium text-white">
              {form.name.charAt(0)}
            </div>
            <div>
              <h3 className="text-base">{form.name}</h3>
              <p className="text-sm text-text-secondary">{form.email}</p>
            </div>
          </div>

          {!isEditing && (
            <button
              onClick={() => setIsEditing(true)}
              className="btn-secondary inline-flex items-center gap-2"
            >
              <Pencil className="h-4 w-4" />
              Editar perfil
            </button>
          )}
        </div>

        {isEditing && (
          <div className="mt-6 border-t border-border pt-6">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <label className="block">
                <span className="text-sm text-text-secondary">
                  Nombre completo
                </span>
                <input
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </label>

              <label className="block">
                <span className="text-sm text-text-secondary">
                  Correo electrónico
                </span>
                <input
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  className="mt-1 w-full rounded-lg border border-border bg-surface px-4 py-2 text-sm focus:border-primary focus:outline-none"
                />
              </label>
            </div>

            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setIsEditing(false)}
                className="btn-secondary"
              >
                Cancelar
              </button>
              <button onClick={handleSave} className="btn-primary">
                Guardar cambios
              </button>
            </div>
          </div>
        )}
      </section>

      {/* Perfil de aprendizaje VARK */}
      <section className="card mt-8">
        <h2 className="tracking-tight">Perfil de aprendizaje (VARK)</h2>
        <p className="mt-1 text-sm text-text-secondary">
          Tu estilo dominante:{" "}
          <span className="font-medium text-navy">
            {dominant.style} ({dominant.percentage}%)
          </span>
        </p>

        {/* Barra segmentada: un tramo por estilo, ancho proporcional a la afinidad */}
        <div className="mt-5 flex h-2 w-full overflow-hidden rounded-full bg-border">
          {varkProfile.map((item) => (
            <div
              key={item.style}
              className={varkSolidClass[item.style]}
              style={{ width: `${item.percentage}%` }}
            />
          ))}
        </div>

        <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
          {varkProfile.map((item) => (
            <div key={item.style} className="flex items-center gap-2">
              <span
                className={`h-2.5 w-2.5 shrink-0 rounded-full ${varkSolidClass[item.style]}`}
              />
              <span className="text-sm text-text-secondary">
                {item.style} · {item.percentage}%
              </span>
            </div>
          ))}
        </div>
      </section>
    </Layout>
  );
}

export default Profile;

import { useEffect, useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";

import Layout from "../components/Layout";
import { getMyVarkResult } from "../api/varkService";
import { varkSolidClass, varkEnumToLabel } from "../utils/vark";

function VarkResult() {
  const location = useLocation();
  const navigate = useNavigate();

  const [result, setResult] = useState(location.state?.result ?? null);
  const [isLoading, setIsLoading] = useState(!location.state?.result);
  const [error, setError] = useState("");

  useEffect(() => {
    if (result) return;

    getMyVarkResult()
      .then(setResult)
      .catch(() =>
        setError(
          "Todavía no tienes un resultado guardado. Resuelve el test primero.",
        ),
      )
      .finally(() => setIsLoading(false));
  }, [result]);

  if (isLoading) {
    return (
      <Layout>
        <p className="text-text-secondary">Cargando tu resultado...</p>
      </Layout>
    );
  }

  if (error || !result) {
    return (
      <Layout>
        <p className="text-text-secondary">{error}</p>
        <Link to="/vark-test" className="btn-primary mt-4 inline-flex">
          Ir al test
        </Link>
      </Layout>
    );
  }

  const scores = [
    { style: "Visual", value: result.visualScore },
    { style: "Auditivo", value: result.auditoryScore },
    { style: "Lectura/Escritura", value: result.readingWritingScore },
    { style: "Kinestésico", value: result.kinestheticScore },
  ];

  const total = scores.reduce((sum, s) => sum + s.value, 0) || 1;
  const dominantLabel =
    varkEnumToLabel[result.dominantStyle] ?? result.dominantStyle;

  return (
    <Layout>
      <section className="card flex flex-col items-center py-10 text-center">
        <span className="badge badge-success">Resultado guardado</span>

        <h1 className="mt-4 tracking-tight">
          Tu estilo dominante es {dominantLabel}
        </h1>
        <p className="mt-1 max-w-md text-sm text-text-secondary">
          A partir de ahora, el contenido de tus cursos se priorizará según este
          estilo cuando esté disponible.
        </p>

        <div className="mt-8 w-full max-w-md">
          <div className="flex h-2 w-full overflow-hidden rounded-full bg-border">
            {scores.map((item) => (
              <div
                key={item.style}
                className={varkSolidClass[item.style]}
                style={{ width: `${(item.value / total) * 100}%` }}
              />
            ))}
          </div>

          <div className="mt-4 grid grid-cols-2 gap-3">
            {scores.map((item) => (
              <div key={item.style} className="flex items-center gap-2">
                <span
                  className={`h-2.5 w-2.5 shrink-0 rounded-full ${varkSolidClass[item.style]}`}
                />
                <span className="text-sm text-text-secondary">
                  {item.style} · {Math.round((item.value / total) * 100)}%
                </span>
              </div>
            ))}
          </div>
        </div>

        <button
          onClick={() => navigate("/dashboard")}
          className="btn-primary mt-8"
        >
          Ir al Dashboard
        </button>
      </section>
    </Layout>
  );
}

export default VarkResult;

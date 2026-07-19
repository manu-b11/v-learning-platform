import { useState } from "react";
import { Link } from "react-router-dom";

import Layout from "../components/Layout";
import { evaluations } from "../data/evaluations";

const filters = ["Todas", "Pendientes", "En progreso", "Completadas"];

const filterToStatus = {
  Pendientes: "Pendiente",
  "En progreso": "En progreso",
  Completadas: "Completada",
};

const statusBadge = {
  Pendiente: "badge-neutral",
  "En progreso": "badge-blue",
  Completada: "badge-success",
};

function Evaluations() {
  const [activeFilter, setActiveFilter] = useState("Todas");

  const filteredEvaluations = evaluations.filter((ev) =>
    activeFilter === "Todas"
      ? true
      : ev.status === filterToStatus[activeFilter],
  );

  return (
    <Layout>
      {/* Encabezado */}
      <section>
        <h1 className="tracking-tight">Evaluaciones</h1>
        <p className="mt-2 text-text-secondary">
          Revisa tus evaluaciones pendientes y consulta tus resultados.
        </p>
      </section>

      {/* Filtros */}
      <section className="mt-8 flex gap-3 border-b border-border pb-8">
        {filters.map((filter) => (
          <button
            key={filter}
            onClick={() => setActiveFilter(filter)}
            className={`rounded-lg px-5 py-2 text-sm font-medium transition-all duration-200 ${
              activeFilter === filter
                ? "bg-primary text-white shadow-sm"
                : "border border-border bg-surface text-text-secondary hover:border-navy/20 hover:text-navy"
            }`}
          >
            {filter}
          </button>
        ))}
      </section>

      {/* Lista de evaluaciones */}
      <section className="panel mt-8 divide-y divide-border">
        {filteredEvaluations.map((ev) => {
          const isCompleted = ev.status === "Completada";
          const to = isCompleted
            ? `/evaluations/${ev.id}/result`
            : `/evaluations/${ev.id}`;

          return (
            <Link
              key={ev.id}
              to={to}
              className="flex items-center justify-between gap-6 p-5 transition-colors duration-200 hover:bg-background"
            >
              <div>
                <h3 className="text-[15px]">{ev.title}</h3>
                <p className="mt-1 text-sm text-text-secondary">
                  {ev.course} · Vence {ev.dueDate}
                </p>
              </div>

              <div className="flex shrink-0 items-center gap-4">
                {ev.score !== null && (
                  <span className="font-heading text-[15px] font-bold text-navy">
                    {ev.score}%
                  </span>
                )}
                <span className={`badge ${statusBadge[ev.status]}`}>
                  {ev.status}
                </span>
              </div>
            </Link>
          );
        })}

        {filteredEvaluations.length === 0 && (
          <p className="px-5 py-12 text-center text-text-secondary">
            No hay evaluaciones en esta categoría.
          </p>
        )}
      </section>
    </Layout>
  );
}

export default Evaluations;

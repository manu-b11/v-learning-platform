import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  BookOpen,
  Award,
  Clock3,
  FileCheck,
  ChevronRight,
  Sparkles,
} from "lucide-react";

import Layout from "../components/Layout";
import StatCard from "../components/StatCard";
import CourseCard from "../components/CourseCard";
import { courses } from "../data/courses";
import { evaluations } from "../data/evaluations";
import { formatDate } from "../utils/format";
import { getMyVarkResult } from "../api/varkService";

function Dashboard() {
  const [hasVarkResult, setHasVarkResult] = useState(true); // true por defecto: evita el flash del banner mientras se consulta
  const [isCheckingVark, setIsCheckingVark] = useState(true);

  useEffect(() => {
    getMyVarkResult()
      .then(() => setHasVarkResult(true))
      .catch(() => setHasVarkResult(false))
      .finally(() => setIsCheckingVark(false));
  }, []);

  const completedCount = courses.filter(
    (c) => c.status === "Completado",
  ).length;
  const inProgressCount = courses.filter(
    (c) => c.status === "En progreso",
  ).length;

  // Continuar viendo: cursos en progreso, hasta 4
  const continuingCourses = courses
    .filter((c) => c.status === "En progreso")
    .slice(0, 4);

  // Próximos vencimientos: cursos y evaluaciones sin completar, unidos y ordenados por fecha
  const upcoming = [
    ...courses
      .filter((c) => c.status !== "Completado")
      .map((c) => ({
        key: `course-${c.id}`,
        title: c.title,
        type: "Curso",
        dueDate: c.dueDate,
        to: `/courses/${c.id}`,
      })),
    ...evaluations
      .filter((e) => e.status !== "Completada")
      .map((e) => ({
        key: `evaluation-${e.id}`,
        title: e.title,
        type: "Evaluación",
        dueDate: e.dueDate,
        to: `/evaluations/${e.id}`,
      })),
  ]
    .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate))
    .slice(0, 4);

  return (
    <Layout>
      <div className="space-y-8">
        {/* Aviso: solo aparece si el usuario aún no tiene un resultado VARK guardado */}
        {!isCheckingVark && !hasVarkResult && (
          <div className="card flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-start gap-3">
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary-light">
                <Sparkles className="h-5 w-5 text-primary" strokeWidth={1.75} />
              </div>
              <div>
                <p className="text-sm font-medium text-navy">
                  Aún no has realizado tu diagnóstico de estilo de aprendizaje
                </p>
                <p className="mt-1 text-sm text-text-secondary">
                  Responde el test VARK para que tus cursos se adapten a cómo
                  aprendes mejor.
                </p>
              </div>
            </div>
            <Link to="/vark-test" className="btn-primary shrink-0">
              Realizar test VARK
            </Link>
          </div>
        )}

        {/* Banner de bienvenida: a todo el ancho, con más presencia */}
        <div className="flex h-44 flex-col justify-center rounded-xl border border-white/10 bg-navy px-8 shadow-sm sm:px-10">
          <h1 className="font-heading text-[28px] font-semibold tracking-tight text-white">
            ¡Bienvenida, Sara!
          </h1>
          <p className="mt-2 max-w-lg text-[15px] leading-relaxed text-white/70">
            Continúa desarrollando tus competencias y realiza seguimiento a tu
            progreso de aprendizaje.
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 gap-5 sm:grid-cols-4">
          <StatCard
            title="Mis cursos"
            value={courses.length}
            icon={BookOpen}
            iconBg="bg-primary-light"
            iconColor="text-primary"
          />

          <StatCard
            title="Completados"
            value={completedCount}
            icon={Award}
            iconBg="bg-primary-light"
            iconColor="text-primary"
          />

          <StatCard
            title="En progreso"
            value={inProgressCount}
            icon={Clock3}
            iconBg="bg-primary-light"
            iconColor="text-primary"
          />

          <StatCard
            title="Certificados"
            value={completedCount}
            icon={FileCheck}
            iconBg="bg-primary-light"
            iconColor="text-primary"
          />
        </div>

        {/* Continuar viendo + Próximos vencimientos */}
        <div className="flex flex-col gap-8 lg:flex-row lg:items-start">
          <section className="min-w-0 flex-1">
            <div className="mb-5 flex items-center justify-between">
              <h2 className="tracking-tight">Continuar viendo</h2>
              <Link to="/courses" className="btn-secondary">
                Ver todos
              </Link>
            </div>

            {/* @container: el número de columnas depende del ancho real de esta
                sección, no del viewport completo (evita cards apretujadas). */}
            <div className="@container">
              <div className="grid grid-cols-1 gap-6 @lg:grid-cols-2">
                {continuingCourses.map((course) => (
                  <CourseCard key={course.id} course={course} />
                ))}
              </div>
            </div>

            {continuingCourses.length === 0 && (
              <p className="text-sm text-text-secondary">
                No tienes cursos en progreso en este momento.
              </p>
            )}
          </section>

          <aside className="w-full shrink-0 lg:w-96">
            <h2 className="mb-5 tracking-tight">Próximos vencimientos</h2>

            <div className="panel divide-y divide-border">
              {upcoming.map((item) => (
                <Link
                  key={item.key}
                  to={item.to}
                  className="flex items-center justify-between gap-4 p-4 transition-colors duration-200 hover:bg-background"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-navy">
                      {item.title}
                    </p>
                    <p className="mt-0.5 text-xs text-text-secondary">
                      {item.type} · Vence {formatDate(item.dueDate)}
                    </p>
                  </div>
                  <ChevronRight className="h-4 w-4 shrink-0 text-text-secondary" />
                </Link>
              ))}

              {upcoming.length === 0 && (
                <p className="p-4 text-sm text-text-secondary">
                  No tienes vencimientos próximos.
                </p>
              )}
            </div>
          </aside>
        </div>
      </div>
    </Layout>
  );
}

export default Dashboard;

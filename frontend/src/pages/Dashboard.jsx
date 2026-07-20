import { Link } from "react-router-dom";
import { BookOpen, Award, Clock3, FileCheck, ChevronRight } from "lucide-react";

import Layout from "../components/Layout";
import StatCard from "../components/StatCard";
import CourseCard from "../components/CourseCard";
import { courses } from "../data/courses";
import { evaluations } from "../data/evaluations";
import { formatDate } from "../utils/format";

function Dashboard() {
  const recentCourses = courses.slice(0, 3);

  const completedCount = courses.filter(
    (c) => c.status === "Completado",
  ).length;
  const inProgressCount = courses.filter(
    (c) => c.status === "En progreso",
  ).length;

  // Curso a retomar: el que está en progreso, en su primer módulo con contenido pendiente
  const continueCourse = courses.find((c) => c.status === "En progreso");
  const continueModule =
    continueCourse?.modules.find((m) => m.contents.some((c) => !c.completed)) ??
    continueCourse?.modules[0];
  const continueModuleIndex =
    continueCourse?.modules.findIndex((m) => m.id === continueModule?.id) ?? -1;

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
      <div className="flex h-40 flex-col justify-center rounded-xl border border-white/10 bg-navy p-8 shadow-sm">
        <h1 className="font-heading text-[26px] font-semibold tracking-tight text-white">
          ¡Bienvenida, Sara!
        </h1>
        <p className="mt-2 max-w-md text-[14px] text-white/70">
          Continúa desarrollando tus competencias y realiza seguimiento a tu
          progreso de aprendizaje.
        </p>
      </div>

      {/* Continuar aprendiendo */}
      {continueCourse && (
        <div className="card-media mt-6 flex flex-col sm:flex-row">
          <img
            src={continueCourse.image}
            alt={continueCourse.title}
            className="h-40 w-full object-cover sm:h-auto sm:w-56"
          />

          <div className="flex flex-1 flex-col justify-center p-6">
            <p className="text-xs font-medium uppercase tracking-wide text-text-secondary">
              Continuar aprendiendo
            </p>
            <h2 className="mt-1 tracking-tight">{continueCourse.title}</h2>
            <p className="mt-1 text-sm text-text-secondary">
              {continueModule?.title}
              {continueModuleIndex >= 0 &&
                ` · Módulo ${continueModuleIndex + 1} de ${continueCourse.modules.length}`}
            </p>

            <div className="mt-4 max-w-sm">
              <div className="progress-track">
                <div
                  className="progress-fill"
                  style={{ width: `${continueCourse.progress}%` }}
                />
              </div>
            </div>

            <Link
              to={`/courses/${continueCourse.id}/modules/${continueModule?.id}`}
              className="btn-primary mt-5 w-fit"
            >
              Continuar
            </Link>
          </div>
        </div>
      )}

      {/* Stat cards: un solo color, son conteos institucionales, no estilos VARK */}
      <div className="mt-8 grid grid-cols-2 gap-6 sm:grid-cols-4">
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

      {/* Mis cursos + Próximos vencimientos */}
      <div className="mt-10 grid grid-cols-1 gap-6 lg:grid-cols-[2fr_1fr]">
        <section>
          <div className="mb-6 flex items-center justify-between">
            <h2 className="tracking-tight">Mis cursos</h2>
            <Link to="/courses" className="btn-secondary">
              Ver todos
            </Link>
          </div>

          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
            {recentCourses.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        </section>

        <section>
          <h2 className="mb-6 tracking-tight">Próximos vencimientos</h2>

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
        </section>
      </div>
    </Layout>
  );
}

export default Dashboard;

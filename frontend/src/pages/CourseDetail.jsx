import { useParams, Link } from "react-router-dom";
import { CircleCheck, Circle, ChevronRight } from "lucide-react";

import Layout from "../components/Layout";
import Breadcrumbs from "../components/Breadcrumbs";
import ContentTypeIcon from "../utils/ContentTypeIcon";
import { getCourseById } from "../data/courses";
import { varkBadgeClass } from "../utils/vark";
import { getTotalMinutes, getRemainingMinutes } from "../utils/courseStats";
import { formatDuration } from "../utils/format";

function CourseDetail() {
  const { id } = useParams();
  const course = getCourseById(id);

  if (!course) {
    return (
      <Layout>
        <p className="text-text-secondary">Curso no encontrado.</p>
      </Layout>
    );
  }

  const firstModuleId = course.modules[0]?.id;
  const remainingMinutes = getRemainingMinutes(course);
  const totalMinutes = getTotalMinutes(course);

  return (
    <Layout>
      <Breadcrumbs
        items={[
          { label: "Dashboard", to: "/dashboard" },
          { label: "Mis cursos", to: "/courses" },
          { label: course.title },
        ]}
      />

      {/* Encabezado del curso */}
      <section className="card-media mt-4">
        <img
          src={course.image}
          alt={course.title}
          className="h-56 w-full object-cover"
        />

        <div className="p-6">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <span
                  className={`badge ${varkBadgeClass[course.learningStyle]}`}
                >
                  {course.learningStyle}
                </span>
                <span className="badge badge-neutral">{course.level}</span>
              </div>
              <h1 className="mt-3 tracking-tight">{course.title}</h1>
              <p className="mt-1 text-sm text-text-secondary">
                {course.instructor}
              </p>
            </div>

            <Link
              to={`/courses/${course.id}/modules/${firstModuleId}`}
              className="btn-primary shrink-0"
            >
              {course.progress > 0 ? "Continuar" : "Comenzar"}
            </Link>
          </div>

          <p className="mt-4 max-w-2xl text-sm leading-relaxed text-text-secondary">
            {course.description}
          </p>

          {/* Objetivos del curso */}
          <div className="mt-6 max-w-2xl">
            <h3 className="text-[15px]">Al terminar este curso, podrás:</h3>
            <ul className="mt-2 space-y-1.5">
              {course.objectives.map((objective) => (
                <li
                  key={objective}
                  className="flex items-start gap-2 text-sm text-text-secondary"
                >
                  <span className="mt-2 h-1 w-1 shrink-0 rounded-full bg-text-secondary" />
                  {objective}
                </li>
              ))}
            </ul>
          </div>

          {/* Progreso + tiempo restante */}
          <div className="mt-6 max-w-md">
            <div className="mb-2 flex items-center justify-between">
              <span className="text-sm text-text-secondary">
                {course.status === "Completado"
                  ? `Completado · ${formatDuration(totalMinutes)} en total`
                  : `${formatDuration(remainingMinutes)} restantes`}
              </span>
              <span className="text-sm font-medium text-navy">
                {course.progress}%
              </span>
            </div>
            <div className="progress-track">
              <div
                className="progress-fill"
                style={{ width: `${course.progress}%` }}
              />
            </div>
          </div>
        </div>
      </section>

      {/* Módulos */}
      <section className="mt-8">
        <h2 className="tracking-tight">Módulos</h2>

        <div className="panel mt-4 divide-y divide-border">
          {course.modules.map((module) => {
            const done = module.contents.filter((c) => c.completed).length;
            const total = module.contents.length;
            const isComplete = done === total;
            const contentTypes = [
              ...new Set(module.contents.map((c) => c.type)),
            ];

            return (
              <Link
                key={module.id}
                to={`/courses/${course.id}/modules/${module.id}`}
                className="flex items-center justify-between gap-6 p-5 transition-colors duration-200 hover:bg-background"
              >
                <div className="flex items-center gap-4">
                  {isComplete ? (
                    <CircleCheck className="h-5 w-5 shrink-0 text-success" />
                  ) : (
                    <Circle className="h-5 w-5 shrink-0 text-border" />
                  )}
                  <div>
                    <h3 className="text-[15px]">{module.title}</h3>
                    <div className="mt-1 flex items-center gap-3">
                      <div className="flex items-center gap-1.5">
                        {contentTypes.map((type) => (
                          <ContentTypeIcon
                            key={type}
                            type={type}
                            className="h-3.5 w-3.5 text-text-secondary"
                          />
                        ))}
                      </div>
                      <p className="text-sm text-text-secondary">
                        {done}/{total} contenidos completados
                      </p>
                    </div>
                  </div>
                </div>
                <ChevronRight className="h-5 w-5 shrink-0 text-text-secondary" />
              </Link>
            );
          })}
        </div>
      </section>
    </Layout>
  );
}

export default CourseDetail;

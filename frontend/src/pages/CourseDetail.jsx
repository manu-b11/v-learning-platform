import { useParams, Link } from "react-router-dom";
import { CircleCheck, Circle, ChevronRight } from "lucide-react";

import Layout from "../components/Layout";
import { getCourseById } from "../data/courses";
import { varkBadgeClass } from "../utils/vark";

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

  return (
    <Layout>
      {/* Encabezado del curso */}
      <section className="card-media">
        <img
          src={course.image}
          alt={course.title}
          className="h-56 w-full object-cover"
        />

        <div className="p-6">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <span className={`badge ${varkBadgeClass[course.learningStyle]}`}>
                {course.learningStyle}
              </span>
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

          <div className="mt-6 max-w-md">
            <div className="mb-2 flex items-center justify-between">
              <span className="text-sm text-text-secondary">
                Progreso general
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
                    <p className="mt-1 text-sm text-text-secondary">
                      {done}/{total} contenidos completados
                    </p>
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

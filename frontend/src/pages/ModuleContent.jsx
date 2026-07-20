import { useState } from "react";
import { useParams } from "react-router-dom";
import { CircleCheck, Circle, Info } from "lucide-react";

import Layout from "../components/Layout";
import Breadcrumbs from "../components/Breadcrumbs";
import ContentPlayer from "../components/ContentPlayer";
import ContentTypeIcon from "../utils/ContentTypeIcon";
import { getCourseById, getModuleById } from "../data/courses";
import { formatDuration } from "../utils/format";

function ModuleContent() {
  const { id, moduleId } = useParams();
  const course = getCourseById(id);
  const module = getModuleById(course, moduleId);

  // si no hay contenido para el estilo del usuario,
  // se hace fallback al contenido de tipo Lectura/Escritura como respaldo
  const preferred = module?.contents.find(
    (c) => c.style === course?.learningStyle,
  );
  const fallback = module?.contents.find(
    (c) => c.style === "Lectura/Escritura",
  );
  const defaultContent = preferred || fallback || module?.contents[0];

  const [selectedId, setSelectedId] = useState(defaultContent?.id);
  const [isFallback, setIsFallback] = useState(Boolean(!preferred && fallback));

  if (!course || !module) {
    return (
      <Layout>
        <p className="text-text-secondary">Contenido no encontrado.</p>
      </Layout>
    );
  }

  const selectedContent =
    module.contents.find((c) => c.id === selectedId) ?? defaultContent;

  const currentIndex = module.contents.findIndex(
    (c) => c.id === selectedContent.id,
  );
  const nextContent = module.contents[currentIndex + 1];
  const isRecommended = selectedContent.style === course.learningStyle;

  function handleSelect(content) {
    setSelectedId(content.id);
    setIsFallback(false);
  }

  return (
    <Layout>
      <Breadcrumbs
        items={[
          { label: "Dashboard", to: "/dashboard" },
          { label: "Mis cursos", to: "/courses" },
          { label: course.title, to: `/courses/${course.id}` },
          { label: module.title },
        ]}
      />

      <h1 className="mt-2 tracking-tight">{module.title}</h1>

      {isFallback && (
        <div className="mt-4 flex items-start gap-2 rounded-lg border border-border bg-background px-4 py-3 text-sm text-text-secondary">
          <Info className="h-4 w-4 shrink-0 translate-y-0.5" />
          <p>
            Este módulo no tiene contenido en tu estilo{" "}
            <strong className="font-medium text-navy">
              {course.learningStyle}
            </strong>
            , así que te mostramos la versión de lectura como alternativa.
          </p>
        </div>
      )}

      <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-[2fr_1fr]">
        <ContentPlayer
          content={selectedContent}
          isRecommended={isRecommended}
          onNext={nextContent ? () => handleSelect(nextContent) : null}
        />

        <div className="panel h-fit divide-y divide-border">
          {module.contents.map((content) => (
            <button
              key={content.id}
              onClick={() => handleSelect(content)}
              className={`flex w-full items-center gap-3 p-4 text-left transition-colors duration-200 ${
                content.id === selectedContent.id
                  ? "bg-background"
                  : "hover:bg-background"
              }`}
            >
              <ContentTypeIcon
                type={content.type}
                className="h-4 w-4 shrink-0 text-text-secondary"
              />

              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-navy">
                  {content.title}
                </p>
                <p className="mt-0.5 text-xs text-text-secondary">
                  {formatDuration(content.durationMinutes)}
                </p>
              </div>

              {content.completed ? (
                <CircleCheck className="h-4 w-4 shrink-0 text-success" />
              ) : (
                <Circle className="h-4 w-4 shrink-0 text-border" />
              )}
            </button>
          ))}
        </div>
      </div>
    </Layout>
  );
}

export default ModuleContent;

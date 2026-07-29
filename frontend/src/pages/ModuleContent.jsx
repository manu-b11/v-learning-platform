import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { CircleCheck, Circle, Info } from "lucide-react";

import Layout from "../components/Layout";
import Breadcrumbs from "../components/Breadcrumbs";
import ContentPlayer from "../components/ContentPlayer";
import ContentTypeIcon from "../utils/ContentTypeIcon";
import { formatDuration } from "../utils/format";

import { getCourseById } from "../api/courseService";
import { getModuleById } from "../api/moduleService";

function ModuleContent() {
  const { id, moduleId } = useParams();

  const [course, setCourse] = useState(null);
  const [module, setModule] = useState(null);

  const [selectedContent, setSelectedContent] = useState(null);
  const [isFallback, setIsFallback] = useState(false);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        const courseData = await getCourseById(id);
        const moduleData = await getModuleById(moduleId);

        setCourse(courseData);
        setModule(moduleData);

        const preferred = moduleData.contents.find(
          (c) => c.learningStyle === courseData.learningStyle,
        );

        const fallback = moduleData.contents.find(
          (c) => c.learningStyle === "READING_WRITING",
        );

        const initial = preferred || fallback || moduleData.contents[0] || null;

        setSelectedContent(initial);
        setIsFallback(!preferred && Boolean(fallback));
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [id, moduleId]);

  if (loading) {
    return (
      <Layout>
        <p className="text-text-secondary">Cargando...</p>
      </Layout>
    );
  }

  if (!course || !module) {
    return (
      <Layout>
        <p className="text-text-secondary">Contenido no encontrado.</p>
      </Layout>
    );
  }

  const currentIndex = module.contents.findIndex(
    (c) => c.id === selectedContent?.id,
  );

  const nextContent = module.contents[currentIndex + 1];

  const isRecommended = selectedContent?.learningStyle === course.learningStyle;

  function handleSelect(content) {
    setSelectedContent(content);
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
            Este módulo no tiene contenido para tu estilo{" "}
            <strong className="font-medium text-navy">
              {course.learningStyle}
            </strong>
            , así que se muestra el contenido disponible.
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
                selectedContent?.id === content.id
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
                  {formatDuration(content.durationMinutes ?? 0)}
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

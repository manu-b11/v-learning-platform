import { BookOpen, ChevronRight } from "lucide-react";

import { varkBadgeClass } from "../utils/vark";
import { formatDuration } from "../utils/format";

function ContentPlayer({ content, isRecommended, onNext }) {
  if (!content) return null;

  const isTextBased =
    content.type === "reading" || content.type === "interactive";

  return (
    <div className="card-media">
      {content.type === "video" && (
        <video
          controls
          className="h-72 w-full bg-navy object-cover sm:h-96"
          key={content.videoUrl}
        >
          <source src={content.videoUrl} type="video/mp4" />
        </video>
      )}

      {content.type === "pdf" && (
        <iframe
          title={content.title}
          src={content.pdfUrl}
          className="h-72 w-full sm:h-96"
        />
      )}

      {isTextBased && (
        <div className="flex h-40 items-center justify-center bg-background">
          <BookOpen className="h-10 w-10 text-border" />
        </div>
      )}

      <div className="p-6">
        <span className={`badge ${varkBadgeClass[content.style]}`}>
          {content.style}
        </span>

        {isRecommended && (
          <p className="mt-2 text-xs font-medium text-success">
            Recomendado para tu estilo {content.style}
          </p>
        )}

        <h2 className="mt-3">{content.title}</h2>
        <p className="mt-1 text-sm text-text-secondary">
          {formatDuration(content.durationMinutes)}
        </p>

        {isTextBased && (
          <p className="mt-4 text-sm leading-relaxed text-text-secondary">
            {content.text}
          </p>
        )}

        <div className="mt-6 flex items-center justify-between">
          <button className="btn-primary">Marcar como completado</button>

          {onNext && (
            <button
              onClick={onNext}
              className="inline-flex items-center gap-1 text-sm font-medium text-primary transition-colors duration-200 hover:text-navy"
            >
              Siguiente contenido
              <ChevronRight className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

export default ContentPlayer;

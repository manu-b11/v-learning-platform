import {
  ChevronRight,
  ExternalLink,
  Headphones,
  Puzzle,
  ClipboardCheck,
} from "lucide-react";

import { varkBadgeClass, varkEnumToLabel } from "../utils/vark";
import { formatDuration } from "../utils/format";

function ContentPlayer({ content, isRecommended, onNext }) {
  if (!content) return null;

  const styleLabel =
    varkEnumToLabel[content.learningStyle] ?? content.learningStyle;

  return (
    <div className="card-media">
      {content.type === "VIDEO" && (
        <video
          controls
          className="h-72 w-full bg-navy object-cover sm:h-96"
          key={content.url}
        >
          <source src={content.url} />
        </video>
      )}

      {content.type === "PDF" && (
        <iframe
          title={content.title}
          src={content.url}
          className="h-72 w-full sm:h-96"
        />
      )}

      {content.type === "IMAGE" && (
        <img
          src={content.url}
          alt={content.title}
          className="h-72 w-full object-cover sm:h-96"
        />
      )}

      {content.type === "PODCAST" && (
        <div className="flex h-40 flex-col items-center justify-center gap-4 bg-background px-6">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-accent-teal-light">
            <Headphones className="h-6 w-6 text-accent-teal" />
          </div>
          {/* eslint-disable-next-line jsx-a11y/media-has-caption */}
          <audio controls src={content.url} className="w-full max-w-sm" />
        </div>
      )}

      {(content.type === "QUIZ" || content.type === "SIMULATION") && (
        <div className="flex h-40 flex-col items-center justify-center gap-3 bg-background px-6">
          {content.type === "QUIZ" ? (
            <ClipboardCheck className="h-8 w-8 text-text-secondary" />
          ) : (
            <Puzzle className="h-8 w-8 text-text-secondary" />
          )}
          <a
            href={content.url}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-1.5 text-sm font-medium text-primary transition-colors duration-200 hover:text-navy"
          >
            Abrir {content.type === "QUIZ" ? "quiz" : "simulación"}
            <ExternalLink className="h-3.5 w-3.5" />
          </a>
        </div>
      )}

      <div className="p-6">
        <span className={`badge ${varkBadgeClass[styleLabel]}`}>
          {styleLabel}
        </span>

        {isRecommended && (
          <p className="mt-2 text-xs font-medium text-success">
            Recomendado para tu estilo {styleLabel}
          </p>
        )}

        <h2 className="mt-3">{content.title}</h2>
        <p className="mt-1 text-sm text-text-secondary">
          {formatDuration(content.durationMinutes ?? 0)}
        </p>

        {content.description && (
          <p className="mt-4 text-sm leading-relaxed text-text-secondary">
            {content.description}
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

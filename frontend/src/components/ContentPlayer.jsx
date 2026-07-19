import { BookOpen } from "lucide-react";

import { varkBadgeClass } from "../utils/vark";

function ContentPlayer({ content }) {
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

        <h2 className="mt-3">{content.title}</h2>
        <p className="mt-1 text-sm text-text-secondary">{content.duration}</p>

        {isTextBased && (
          <p className="mt-4 text-sm leading-relaxed text-text-secondary">
            {content.text}
          </p>
        )}

        <button className="btn-primary mt-6">Marcar como completado</button>
      </div>
    </div>
  );
}

export default ContentPlayer;

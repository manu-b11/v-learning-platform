import { Link } from "react-router-dom";
import { Clock3 } from "lucide-react";

import { varkBadgeClass } from "../utils/vark";
import { formatDuration } from "../utils/format";

function CourseCard({ course, compact = false }) {
  const { id, title, imageUrl, progress, learningStyle, remainingMinutes } =
    course;

  if (compact) {
    return (
      <Link to={`/courses/${id}`} className="card-media block">
        <img src={imageUrl} alt={title} className="h-24 w-full object-cover" />

        <div className="p-4">
          <span className={`badge ${varkBadgeClass[learningStyle]}`}>
            {learningStyle}
          </span>

          <h3 className="mt-2 truncate text-sm">{title}</h3>

          <div className="mt-3">
            <div className="mb-1.5 flex items-center justify-between">
              <span className="text-xs text-text-secondary">
                {formatDuration(remainingMinutes)} restantes
              </span>

              <span className="text-xs font-medium text-navy">{progress}%</span>
            </div>

            <div className="progress-track">
              <div
                className="progress-fill"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        </div>
      </Link>
    );
  }

  return (
    <Link to={`/courses/${id}`} className="card-media block">
      <img src={imageUrl} alt={title} className="h-40 w-full object-cover" />

      <div className="p-5">
        <span className={`badge ${varkBadgeClass[learningStyle]}`}>
          {learningStyle}
        </span>

        <h3 className="mt-3 text-base">{title}</h3>

        <div className="mt-3 flex items-center gap-2 text-xs text-text-secondary">
          <Clock3 className="h-3.5 w-3.5" />
          {formatDuration(remainingMinutes)} restantes
        </div>

        <div className="mt-5">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm text-text-secondary">
              {formatDuration(remainingMinutes)} restantes
            </span>

            <span className="text-sm font-medium text-navy">{progress}%</span>
          </div>

          <div className="progress-track">
            <div className="progress-fill" style={{ width: `${progress}%` }} />
          </div>
        </div>
      </div>
    </Link>
  );
}

export default CourseCard;

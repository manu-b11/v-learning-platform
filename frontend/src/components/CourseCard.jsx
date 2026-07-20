import { Link } from "react-router-dom";
import { Clock3, Layers } from "lucide-react";

import { varkBadgeClass } from "../utils/vark";
import {
  getModuleCount,
  getTotalMinutes,
  getRemainingMinutes,
} from "../utils/courseStats";
import { formatDuration } from "../utils/format";

const statusBadgeClass = {
  Pendiente: "badge-neutral",
  "En progreso": "badge-blue",
  Completado: "badge-success",
};

function CourseCard({ course }) {
  const { id, title, instructor, image, progress, learningStyle, status } =
    course;

  const totalMinutes = getTotalMinutes(course);
  const remainingMinutes = getRemainingMinutes(course);
  const moduleCount = getModuleCount(course);

  return (
    <Link to={`/courses/${id}`} className="card-media block">
      <img src={image} alt={title} className="h-40 w-full object-cover" />

      {/* Contenido */}
      <div className="p-5">
        <div className="flex flex-wrap items-center gap-2">
          <span className={`badge ${varkBadgeClass[learningStyle]}`}>
            {learningStyle}
          </span>
          <span className={`badge ${statusBadgeClass[status]}`}>{status}</span>
        </div>

        <h3 className="mt-3 text-base">{title}</h3>
        <p className="mt-1 text-sm text-text-secondary">{instructor}</p>

        {/* Duración y número de módulos */}
        <div className="mt-3 flex items-center gap-4 text-xs text-text-secondary">
          <span className="flex items-center gap-1">
            <Clock3 className="h-3.5 w-3.5" />
            {formatDuration(totalMinutes)}
          </span>
          <span className="flex items-center gap-1">
            <Layers className="h-3.5 w-3.5" />
            {moduleCount} módulos
          </span>
        </div>

        {/* Progreso + tiempo restante */}
        <div className="mt-5">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm text-text-secondary">
              {status === "Completado"
                ? "Completado"
                : `${formatDuration(remainingMinutes)} restantes`}
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

import { Link } from "react-router-dom";

import { varkBadgeClass } from "../utils/vark";

function CourseCard({ course }) {
  const { id, title, instructor, image, progress, learningStyle } = course;

  return (
    <Link to={`/courses/${id}`} className="card-media block">
      <img src={image} alt={title} className="h-40 w-full object-cover" />

      {/* Contenido */}
      <div className="p-5">
        <h3 className="text-base">{title}</h3>
        <p className="mt-1 text-sm text-text-secondary">{instructor}</p>

        {/* Estilo de aprendizaje */}
        <div className="mt-4">
          <span className={`badge ${varkBadgeClass[learningStyle]}`}>
            {learningStyle}
          </span>
        </div>

        {/* Progreso */}
        <div className="mt-5">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm text-text-secondary">Progreso</span>
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

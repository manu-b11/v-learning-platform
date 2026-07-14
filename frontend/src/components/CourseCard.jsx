function CourseCard({ course }) {
  const { title, instructor, image, progress, learningStyle } = course;

  const badgeStyles = {
    Visual: "badge-blue",
    Auditivo: "badge-teal",
    "Lectura/Escritura": "badge-amber",
    Kinestésico: "badge-coral",
  };

  return (
    <div className="card overflow-hidden">
      {/* Imagen */}
      <img
        src={image}
        alt={title}
        className="h-44 w-full rounded-lg object-cover"
      />

      {/* Contenido */}
      <div className="mt-5">
        <h3>{title}</h3>

        <p className="mt-1 text-sm text-text-secondary">{instructor}</p>

        {/* Estilo de aprendizaje */}
        <div className="mt-4">
          <span className={`badge ${badgeStyles[learningStyle]}`}>
            {learningStyle}
          </span>
        </div>

        {/* Progreso */}
        <div className="mt-5">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm text-text-secondary">Progreso</span>

            <span className="text-sm font-medium">{progress}%</span>
          </div>

          <div className="progress-track">
            <div className="progress-fill" style={{ width: `${progress}%` }} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default CourseCard;

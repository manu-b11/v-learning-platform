import { useState } from "react";
import { Search } from "lucide-react";

import Layout from "../components/Layout";
import CourseCard from "../components/CourseCard";
import { courses } from "../data/courses";

const filters = ["Todos", "En progreso", "Completados", "Pendientes"];

// Mapea la etiqueta del botón (plural) al valor real de `status` en los datos
const filterToStatus = {
  "En progreso": "En progreso",
  Completados: "Completado",
  Pendientes: "Pendiente",
};

function Courses() {
  const [activeFilter, setActiveFilter] = useState("Todos");
  const [searchTerm, setSearchTerm] = useState("");

  const filteredCourses = courses
    .filter((course) =>
      activeFilter === "Todos"
        ? true
        : course.status === filterToStatus[activeFilter],
    )
    .filter((course) =>
      course.title.toLowerCase().includes(searchTerm.toLowerCase()),
    );

  return (
    <Layout>
      {/* Encabezado */}
      <section className="flex items-end justify-between">
        <div>
          <h1 className="tracking-tight">Mis cursos</h1>

          <p className="mt-2 text-text-secondary">
            Consulta tus cursos y continúa tu proceso de aprendizaje.
          </p>
        </div>

        {/* Buscador */}
        <div className="relative w-80">
          <Search
            size={18}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary"
          />

          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Buscar curso..."
            className="w-full rounded-lg border border-border bg-surface py-2 pl-10 pr-4 focus:border-primary focus:outline-none"
          />
        </div>
      </section>

      {/* Filtros */}
      <section className="mt-8 flex gap-3 border-b border-border pb-8">
        {filters.map((filter) => (
          <button
            key={filter}
            onClick={() => setActiveFilter(filter)}
            className={`rounded-lg px-5 py-2 text-sm font-medium transition-all duration-200 ${
              activeFilter === filter
                ? "bg-primary text-white shadow-sm"
                : "border border-border bg-surface text-text-secondary hover:border-navy/20 hover:text-navy"
            }`}
          >
            {filter}
          </button>
        ))}
      </section>

      {/* Cursos */}
      <section className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {filteredCourses.map((course) => (
          <CourseCard key={course.id} course={course} />
        ))}

        {filteredCourses.length === 0 && (
          <p className="col-span-full py-12 text-center text-text-secondary">
            No se encontraron cursos con estos criterios.
          </p>
        )}
      </section>
    </Layout>
  );
}

export default Courses;

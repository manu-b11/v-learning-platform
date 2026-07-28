import { useEffect, useState } from "react";
import { Search } from "lucide-react";

import Layout from "../components/Layout";
import CourseCard from "../components/CourseCard";
import { getAllCourses } from "../api/courseService";

function Courses() {
  const [courses, setCourses] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAllCourses()
      .then(setCourses)
      .finally(() => setLoading(false));
  }, []);

  const filteredCourses = courses.filter((course) =>
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

      {/* Cursos */}
      <section className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {loading ? (
          <p className="col-span-full text-center text-text-secondary">
            Cargando cursos...
          </p>
        ) : filteredCourses.length > 0 ? (
          filteredCourses.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))
        ) : (
          <p className="col-span-full py-12 text-center text-text-secondary">
            No se encontraron cursos.
          </p>
        )}
      </section>
    </Layout>
  );
}

export default Courses;

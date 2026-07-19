import Layout from "../components/Layout";
import StatCard from "../components/StatCard";
import CourseCard from "../components/CourseCard";
import reactImg from "../assets/react.jpg";
import uxImg from "../assets/ux.jpg";
import scrumImg from "../assets/scrum.jpg";
import { BookOpen, Award, Clock3, FileCheck } from "lucide-react";

const courses = [
  {
    id: 1,
    title: "React desde Cero",
    instructor: "Juan Pérez",
    image: reactImg,
    progress: 70,
    learningStyle: "Visual",
  },
  {
    id: 2,
    title: "Introducción a UX",
    instructor: "Laura Gómez",
    image: uxImg,
    progress: 35,
    learningStyle: "Auditivo",
  },
  {
    id: 3,
    title: "Scrum Fundamentals",
    instructor: "Carlos Ruiz",
    image: scrumImg,
    progress: 100,
    learningStyle: "Kinestésico",
  },
];

function Dashboard() {
  return (
    <Layout>
      {/* Banner: fondo sólido navy, sin degradado ni sombra pesada */}
      <div className="flex h-44 flex-col justify-center rounded-xl border border-white/10 bg-navy p-8 shadow-sm">
        <h1 className="font-heading text-[26px] font-semibold tracking-tight text-white">
          ¡Bienvenida, Sara!
        </h1>
        <p className="mt-2 max-w-md text-[14px] text-white/70">
          Continúa desarrollando tus competencias y realiza seguimiento a tu
          progreso de aprendizaje.
        </p>
      </div>

      {/* Stat cards */}
      <div className="mt-8 grid grid-cols-2 gap-6 sm:grid-cols-4">
        <StatCard
          title="Mis cursos"
          value={12}
          icon={BookOpen}
          iconBg="bg-accent-blue-light"
          iconColor="text-accent-blue"
        />

        <StatCard
          title="Completados"
          value={5}
          icon={Award}
          iconBg="bg-accent-blue-light"
          iconColor="text-accent-blue"
        />

        <StatCard
          title="En progreso"
          value={3}
          icon={Clock3}
          iconBg="bg-accent-blue-light"
          iconColor="text-accent-blue"
        />

        <StatCard
          title="Certificados"
          value={2}
          icon={FileCheck}
          iconBg="bg-accent-blue-light"
          iconColor="text-accent-blue"
        />
      </div>

      {/* Cursos */}
      <section className="mt-10">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="tracking-tight">Mis cursos</h2>
          <button className="btn-secondary">Ver todos</button>
        </div>

        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {courses.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
        </div>
      </section>
    </Layout>
  );
}

export default Dashboard;

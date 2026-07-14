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
      {/* Banner */}
      <div className="flex flex-col justify-center rounded-xl bg-primary p-8  h-44 shadow-md">
        <h1 className="text-white">¡Bienvenida, Sara!</h1>

        <p className="mt-2 text-white/90">
          Continúa desarrollando tus competencias y realiza seguimiento a tu
          progreso de aprendizaje.
        </p>
      </div>

      {/* Tarjetas */}
      <div className="mt-8 grid grid-cols-4 gap-6">
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
          iconBg="bg-accent-teal-light"
          iconColor="text-accent-teal"
        />

        <StatCard
          title="En progreso"
          value={3}
          icon={Clock3}
          iconBg="bg-accent-coral-light"
          iconColor="text-accent-coral"
        />

        <StatCard
          title="Certificados"
          value={2}
          icon={FileCheck}
          iconBg="bg-accent-amber-light"
          iconColor="text-accent-amber"
        />
      </div>

      <section className="mt-10">
        <div className="mb-6 flex items-center justify-between">
          <h2>Mis cursos</h2>

          <button className="btn-secondary">Ver todos</button>
        </div>

        <div className="grid grid-cols-3 gap-6">
          {courses.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
        </div>
      </section>
    </Layout>
  );
}

export default Dashboard;

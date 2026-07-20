import reactImg from "../assets/react.jpg";
import uxImg from "../assets/ux.jpg";
import scrumImg from "../assets/scrum.jpg";

// si el estilo del usuario no tiene recurso disponible,
// se muestra el contenido "Lectura/Escritura" como respaldo
function buildModules(topic) {
  return [
    {
      id: 1,
      title: `Fundamentos de ${topic}`,
      contents: [
        {
          id: 1,
          title: `Introducción a ${topic}`,
          type: "video",
          style: "Visual",
          durationMinutes: 10,
          completed: true,
          videoUrl: "https://www.w3schools.com/html/mov_bbb.mp4",
        },
        {
          id: 2,
          title: "Guía de referencia",
          type: "pdf",
          style: "Lectura/Escritura",
          durationMinutes: 12,
          completed: true,
          pdfUrl:
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
        },
        {
          id: 3,
          title: "Ejercicio guiado",
          type: "interactive",
          style: "Kinestésico",
          durationMinutes: 15,
          completed: false,
          text: "Sigue los pasos propuestos y aplica lo aprendido en un entorno práctico, a tu propio ritmo.",
        },
      ],
    },
    {
      id: 2,
      title: `Práctica avanzada de ${topic}`,
      contents: [
        {
          id: 1,
          title: "Caso de estudio",
          type: "video",
          style: "Visual",
          durationMinutes: 14,
          completed: false,
          videoUrl: "https://www.w3schools.com/html/mov_bbb.mp4",
        },
        {
          id: 2,
          title: "Lectura complementaria",
          type: "reading",
          style: "Lectura/Escritura",
          durationMinutes: 8,
          completed: false,
          text: "Contenido de lectura complementaria sobre el tema, con ejemplos aplicados a un contexto laboral real.",
        },
      ],
    },
  ];
}

function buildObjectives(topic) {
  return [
    `Comprender los conceptos fundamentales de ${topic}.`,
    `Aplicar ${topic} en un caso práctico guiado.`,
    `Evaluar tu propio avance mediante ejercicios y una evaluación final.`,
  ];
}

export const courses = [
  {
    id: 1,
    title: "React desde Cero",
    instructor: "Juan Pérez",
    image: reactImg,
    description:
      "Aprende los fundamentos de React: componentes, props, estado y hooks, a través de ejemplos guiados.",
    objectives: buildObjectives("React"),
    level: "Básico",
    dueDate: "2026-08-15",
    progress: 70,
    learningStyle: "Visual",
    status: "En progreso",
    modules: buildModules("React"),
  },
  {
    id: 2,
    title: "Diseño UX",
    instructor: "Laura Gómez",
    image: uxImg,
    description:
      "Principios de diseño centrado en el usuario, investigación y prototipado.",
    objectives: buildObjectives("Diseño UX"),
    level: "Intermedio",
    dueDate: "2026-07-25",
    progress: 100,
    learningStyle: "Auditivo",
    status: "Completado",
    modules: buildModules("Diseño UX"),
  },
  {
    id: 3,
    title: "Scrum Fundamentals",
    instructor: "Carlos Ruiz",
    image: scrumImg,
    description: "Fundamentos del marco Scrum para equipos ágiles.",
    objectives: buildObjectives("Scrum"),
    level: "Básico",
    dueDate: "2026-07-30",
    progress: 35,
    learningStyle: "Kinestésico",
    status: "En progreso",
    modules: buildModules("Scrum"),
  },
  {
    id: 4,
    title: "Java Básico",
    instructor: "María Díaz",
    image: reactImg,
    description: "Introducción a la programación con Java.",
    objectives: buildObjectives("Java"),
    level: "Básico",
    dueDate: "2026-09-01",
    progress: 0,
    learningStyle: "Lectura/Escritura",
    status: "Pendiente",
    modules: buildModules("Java"),
  },
  {
    id: 5,
    title: "Bases de Datos",
    instructor: "Luis Torres",
    image: uxImg,
    description: "Modelado relacional y SQL aplicado.",
    objectives: buildObjectives("Bases de Datos"),
    level: "Intermedio",
    dueDate: "2026-08-05",
    progress: 45,
    learningStyle: "Visual",
    status: "En progreso",
    modules: buildModules("Bases de Datos"),
  },
  {
    id: 6,
    title: "Spring Boot",
    instructor: "Carlos Mejía",
    image: scrumImg,
    description: "Construcción de APIs REST con Spring Boot.",
    objectives: buildObjectives("Spring Boot"),
    level: "Avanzado",
    dueDate: "2026-08-20",
    progress: 80,
    learningStyle: "Auditivo",
    status: "En progreso",
    modules: buildModules("Spring Boot"),
  },
];

export function getCourseById(id) {
  return courses.find((c) => String(c.id) === String(id));
}

export function getModuleById(course, moduleId) {
  return course?.modules.find((m) => String(m.id) === String(moduleId));
}

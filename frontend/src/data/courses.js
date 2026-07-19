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
          duration: "10 min",
          completed: true,
          videoUrl: "https://www.w3schools.com/html/mov_bbb.mp4",
        },
        {
          id: 2,
          title: "Guía de referencia",
          type: "pdf",
          style: "Lectura/Escritura",
          duration: "6 páginas",
          completed: true,
          pdfUrl:
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
        },
        {
          id: 3,
          title: "Ejercicio guiado",
          type: "interactive",
          style: "Kinestésico",
          duration: "15 min",
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
          duration: "14 min",
          completed: false,
          videoUrl: "https://www.w3schools.com/html/mov_bbb.mp4",
        },
        {
          id: 2,
          title: "Lectura complementaria",
          type: "reading",
          style: "Lectura/Escritura",
          duration: "5 min de lectura",
          completed: false,
          text: "Contenido de lectura complementaria sobre el tema, con ejemplos aplicados a un contexto laboral real.",
        },
      ],
    },
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

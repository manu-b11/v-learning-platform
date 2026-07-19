export const evaluations = [
  {
    id: 1,
    title: "Evaluación Módulo 1 — Componentes",
    course: "React desde Cero",
    dueDate: "20 Jul 2026",
    status: "Pendiente",
    score: null,
    questions: [
      {
        id: 1,
        text: "¿Qué hook se usa para manejar estado local en un componente funcional?",
        options: ["useEffect", "useState", "useContext", "useRef"],
        correctIndex: 1,
      },
      {
        id: 2,
        text: "¿Qué prop especial identifica de forma única a un elemento dentro de una lista?",
        options: ["id", "key", "index", "ref"],
        correctIndex: 1,
      },
      {
        id: 3,
        text: "¿Cómo se le pasa un valor de un componente padre a uno hijo?",
        options: [
          "Mediante state interno",
          "Únicamente con Context API",
          "Mediante props",
          "Mediante LocalStorage",
        ],
        correctIndex: 2,
      },
    ],
  },
  {
    id: 2,
    title: "Quiz de Fundamentos de UX",
    course: "Diseño UX",
    dueDate: "10 Jul 2026",
    status: "Completada",
    score: 92,
    questions: [],
  },
  {
    id: 3,
    title: "Examen Final",
    course: "Scrum Fundamentals",
    dueDate: "15 Jul 2026",
    status: "En progreso",
    score: null,
    questions: [
      {
        id: 1,
        text: "¿Cuánto dura habitualmente un Sprint?",
        options: ["1 día", "1 a 4 semanas", "6 meses", "No tiene duración fija"],
        correctIndex: 1,
      },
      {
        id: 2,
        text: "¿Quién es responsable de maximizar el valor del producto?",
        options: [
          "Scrum Master",
          "Product Owner",
          "Equipo de desarrollo",
          "Stakeholders",
        ],
        correctIndex: 1,
      },
    ],
  },
  {
    id: 4,
    title: "Evaluación Diagnóstica",
    course: "Java Básico",
    dueDate: "5 Jul 2026",
    status: "Completada",
    score: 78,
    questions: [],
  },
];

export function getEvaluationById(id) {
  return evaluations.find((e) => String(e.id) === String(id));
}

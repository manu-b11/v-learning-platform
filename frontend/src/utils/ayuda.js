/**
 * RF-048: ayuda de cada página. Se elige por la ruta; si la página no tiene ayuda propia se
 * muestra la ayuda general (excepción 1 del CU-048).
 */
const AYUDAS = [
  [/^\/inicio$/, "Inicio", [
    "Aquí ves lo que tienes pendiente y tus cursos recientes.",
    "Usa «Ver más» en cada bloque para ir a la lista completa.",
  ]],
  [/^\/cursos\/\d+\/editar$/, "Editor de curso", [
    "Organiza el curso en módulos y agrega contenidos y evaluaciones en cada uno.",
    "Cada contenido muestra una revisión de accesibilidad mientras escribes: corrige lo que bloquea la publicación.",
    "Publica el curso cuando sus contenidos estén publicados y pasen la revisión.",
  ]],
  [/^\/cursos\/\d+\/notas$/, "Notas del curso", [
    "La tabla muestra la nota de cada estudiante en cada actividad y su nota final.",
    "Con «Acomodaciones» asignas más tiempo, tiempo ilimitado o una fecha especial a un estudiante en todas las evaluaciones del curso.",
  ]],
  [/^\/cursos\/\d+$/, "Detalle del curso", [
    "Los módulos están en orden. Abre un contenido para estudiarlo y márcalo como completado.",
    "Las evaluaciones de un módulo se habilitan cuando completas al menos un contenido.",
  ]],
  [/^\/cursos$/, "Cursos", [
    "Busca un curso por su nombre o revisa los que ya tienes.",
  ]],
  [/^\/contenidos\/\d+$/, "Lección", [
    "Usa las pestañas para ver el contenido, el apoyo cognitivo o el texto alternativo.",
    "Puedes pedir que te lean el texto en voz alta y activar subtítulos o la transcripción.",
    "Si algo no se entiende, pregúntale al Tutor de IA desde este mismo botón de ayuda.",
  ]],
  [/^\/intentos\/\d+$/, "Evaluación", [
    "Cada respuesta se guarda sola al elegirla o escribirla. Puedes salir y continuar después.",
    "Si la evaluación tiene tiempo, te avisamos cuando queden 2 minutos.",
  ]],
  [/^\/evaluaciones$/, "Evaluaciones", [
    "Aquí ves tus evaluaciones, el tiempo que tienes y tus mejores resultados.",
  ]],
  [/^\/progreso$/, "Mi progreso", [
    "Cada bloque muestra tu avance y tiene una tabla con los mismos datos.",
    "Puedes exportar tu progreso en PDF o Excel.",
  ]],
  [/^\/comunidad(\/\d+)?$/, "Comunidad", [
    "Escribe en el chat del curso para hablar con tus compañeros y tu instructor.",
    "Los mensajes nuevos se anuncian al lector de pantalla sin mover el foco.",
  ]],
  [/^\/accesibilidad$/, "Accesibilidad", [
    "Elige tamaño de letra, tipografía, contraste, subtítulos, lectura en voz alta y tiempo adicional.",
    "Los cambios se guardan en tu perfil y se aplican en todas tus sesiones.",
  ]],
];

const GENERAL = ["Ayuda general", [
  "Usa el menú lateral para moverte entre secciones. Con Tab recorres los controles y con Enter los activas.",
  "Pulsa «?» para ver los atajos de teclado.",
  "En «Accesibilidad» puedes ajustar la plataforma a tu forma de aprender.",
]];

/** { titulo, puntos, propia } con la ayuda de la ruta o la general. */
export function ayudaDe(ruta) {
  const encontrada = AYUDAS.find(([patron]) => patron.test(ruta));
  if (!encontrada) return { titulo: GENERAL[0], puntos: GENERAL[1], propia: false };
  return { titulo: encontrada[1], puntos: encontrada[2], propia: true };
}

/** Curso de la ruta actual, si se puede saber (para escribir al instructor de ese curso). */
export function cursoDeRuta(ruta) {
  const m = ruta.match(/^\/(?:cursos|comunidad)\/(\d+)/);
  return m ? m[1] : null;
}

/**
 * Respuestas simuladas de la API para las pruebas de accesibilidad. Siguen la forma de los
 * records de backend/learning-platform/src/main/java/com/elearning/platform/dto.
 */
const AHORA = "2026-11-10T10:00:00";

export const ESTUDIANTE = {
  id: 20, nombre: "Sofía Restrepo", correo: "sofia@tdea.edu.co", rol: "ESTUDIANTE", estado: "ACTIVO",
  primerAcceso: false, varkCompletado: true, accesibilidadConfigurada: true, programaAcademico: "Ingeniería de Software",
  dobleFactorHabilitado: true, dobleFactorObligatorio: false,
};

export const INSTRUCTOR = {
  id: 10, nombre: "Inés Gómez", correo: "ines@tdea.edu.co", rol: "INSTRUCTOR", estado: "ACTIVO",
  primerAcceso: false, varkCompletado: null, accesibilidadConfigurada: null, programaAcademico: null,
  dobleFactorHabilitado: true, dobleFactorObligatorio: true,
};

const CONFIG = {
  altoContraste: false, tamanoFuente: 16, tipografia: null, espaciadoLinea: 1.5, navegacionTeclado: false,
  lectorPantalla: false, subtitulos: true, transcripcion: true, textoAVoz: true, tiempoAdicional: 0,
};

const RECURSOS = [
  { id: 1, tipo: "SUBTITULO", url: "/sub.vtt", descripcion: null, disponible: true },
  { id: 2, tipo: "TRANSCRIPCION", url: null, descripcion: "Bienvenidos al curso. Hoy veremos los roles de Scrum.", disponible: true },
];

const CONTENIDO_ITEM = {
  id: 1, titulo: "Roles de Scrum", formato: "LECTURA", duracionMinutos: 10, publicado: true, afin: true,
  estado: "EN_PROGRESO", porcentaje: 40, conforme: true, faltantes: [],
};

const CURSO = {
  id: 5, titulo: "Metodologías ágiles", descripcion: "Scrum y Kanban para equipos de software.", imagen: null,
  instructor: "Inés Gómez", estado: "PUBLICADO", inscrito: true, porcentaje: 40, metodoPrincipal: "R", metodoSecundario: "V",
  modulos: [{
    id: 3, titulo: "Fundamentos", descripcion: "Qué es Scrum.", orden: 1, sinFormatoAfin: false, completo: false,
    contenidos: [CONTENIDO_ITEM],
    evaluaciones: [{
      id: 7, titulo: "Quiz de roles", tipo: "QUIZ", calificable: true, aviso: null, fechaLimite: null,
      puntajeMaximo: 10, tiempoLimite: 20, descripcion: "Cinco preguntas.",
    }],
  }],
};

const DASHBOARD = {
  sinActividad: false, mensaje: null,
  totales: { tiempoTotalSegundos: 5400, contenidosCompletados: 4, evaluacionesRealizadas: 2, promedioCalificacion: 85, tareasCalificadas: 1 },
  cursos: [{ cursoId: 5, titulo: "Metodologías ágiles", imagen: null, porcentaje: 40, estado: "ACTIVA",
    modulosCompletos: 1, modulosTotales: 3, contenidosCompletados: 4, tiempoConsumidoSegundos: 5400 }],
  calificaciones: [{ evaluacionId: 7, evaluacion: "Quiz de roles", curso: "Metodologías ágiles", mejorPorcentaje: 90,
    intentos: 2, clasificacion: "EXCELENTE", tipo: "EVALUACION" }],
  pendientes: [{ tipo: "CONTENIDO", id: 1, titulo: "Roles de Scrum", curso: "Metodologías ágiles", cursoId: 5, moduloId: 3 }],
  sugerencias: [], cursosSugeridos: [],
  resumenes: {
    avance: "Tienes 1 curso: «Metodologías ágiles» con 40 % de avance.",
    calificaciones: "Tienes 1 actividad(es) calificada(s) con un promedio de 90 %. Tu mejor resultado es «Quiz de roles» con 90 %.",
  },
};

const LECCION = {
  id: 1, cursoId: 5, titulo: "Roles de Scrum", descripcion: "Los tres roles del equipo Scrum.", formato: "LECTURA",
  urlRecurso: null, cuerpo: "## Product Owner\n\nPrioriza el backlog.\n\n## Scrum Master\n\nFacilita el proceso.",
  duracionMinutos: 10, recursos: RECURSOS, opcionesAlternativas: [], lenguaSenasDisponible: false,
  mensajeLenguaSenas: null, apoyoCognitivo: true, advertencias: [], estado: "EN_PROGRESO", porcentaje: 40, tiempoConsumido: 120,
};

export const INTENTO = {
  id: 9, evaluacionId: 7, titulo: "Quiz de roles", tipo: "QUIZ", estado: "EN_PROGRESO", fechaInicio: AHORA,
  tiempoLimiteMinutos: 20, segundosRestantes: 1100, reanudado: false, minutosAmpliacionDisponibles: 0, respuestas: {},
  preguntas: [{
    id: 1, enunciado: "¿Quién prioriza el backlog?", tipo: "OPCION_UNICA", puntaje: 2, orden: 1,
    opciones: [{ id: 11, texto: "Product Owner" }, { id: 12, texto: "Scrum Master" }],
  }],
};

const MENSAJES = [
  { id: 1, cursoId: 5, usuarioId: 10, autor: "Inés Gómez", rol: "INSTRUCTOR", contenido: "Bienvenidos al curso.", fechaEnvio: AHORA, estado: "ENVIADO" },
  { id: 2, cursoId: 5, usuarioId: 20, autor: "Sofía Restrepo", rol: "ESTUDIANTE", contenido: "¡Gracias!", fechaEnvio: AHORA, estado: "ENVIADO" },
];

const INFORME = {
  contenidoId: 1, titulo: "Roles de Scrum", bloqueantes: 1, advertencias: 1, fechaVerificacion: AHORA,
  hallazgos: [
    { contenidoId: 1, contenido: "Roles de Scrum", regla: "IMAGEN_SIN_ALT", criterioWcag: "1.1.1", severidad: "BLOQUEANTE",
      mensaje: "La imagen «diagrama.png» no tiene texto alternativo.", comoCorregir: "Describe la imagen entre los corchetes." },
    { contenidoId: 1, contenido: "Roles de Scrum", regla: "ENLACE_NO_DESCRIPTIVO", criterioWcag: "2.4.4", severidad: "ADVERTENCIA",
      mensaje: "El enlace «aquí» no dice a dónde lleva.", comoCorregir: "Escribe a dónde lleva el enlace." },
  ],
};

/** [método, expresión de la ruta, cuerpo] — la primera que coincide responde. */
function rutas(usuario) {
  return [
    ["GET", /\/auth\/me$/, usuario],
    ["POST", /\/auth\/login$/, { estado: "OTP_REQUERIDO", mensaje: "Enviamos un código de 6 números a tu correo. Vence en 10 minutos.", codigoOtp: null, sesion: null }],
    ["GET", /\/accesibilidad\/perfil$/, { configurado: true, categorias: [], configuracion: CONFIG, fechaActualizacion: AHORA }],
    ["GET", /\/accesibilidad\/configuracion$/, CONFIG],
    ["GET", /\/notificaciones(\?.*)?$/, { noLeidas: 0, notificaciones: [] }],
    ["GET", /\/logros(\?.*)?$/, { puntos: 120, nivel: 2, puntosSiguienteNivel: 200, progresoNivelPorcentaje: 60, mostrarEnRanking: true, insignias: [], pendientes: [] }],
    ["GET", /\/tutor\/historial/, []],
    ["GET", /\/progreso$/, DASHBOARD],
    ["GET", /\/mis-cursos$/, [{ ...CURSO, modulos: undefined }]],
    ["GET", /\/instructor\/cursos$/, [{ ...CURSO, modulos: undefined }]],
    ["GET", /\/instructor\/cursos\/5$/, CURSO],
    ["GET", /\/cursos\/5$/, CURSO],
    ["GET", /\/contenidos\/1$/, LECCION],
    ["GET", /\/contenidos\/1\/apoyo-cognitivo$/, { unidades: [], pasos: [], glosario: [] }],
    ["GET", /\/contenidos\/1\/recursos$/, RECURSOS],
    ["GET", /\/contenidos\/1\/conformidad$/, { conforme: true, faltantes: [], advertencias: [] }],
    ["POST", /\/contenidos\/1\/accesibilidad\/verificar$/, INFORME],
    ["POST", /\/contenidos\/1\/progreso$/, { porcentaje: 40, estado: "EN_PROGRESO" }],
    ["POST", /\/evaluaciones\/7\/intentos$/, INTENTO],
    ["GET", /\/cursos\/5\/mensajes/, MENSAJES],
    ["GET", /\/cursos\/5\/comunidad\/configuracion$/, { colaReenvio: true }],
    ["GET", /\/cursos\/5\/acomodaciones$/, []],
  ];
}

/** Simula toda la API bajo /api; lo que no está previsto responde 200 vacío para no romper la vista. */
export async function simularApi(page, usuario) {
  const tabla = rutas(usuario);
  // Solo las peticiones a /api/…: un patrón «**/api/**» también atraparía el módulo /src/api/client.js.
  await page.route((url) => url.pathname.startsWith("/api/"), async (route) => {
    const req = route.request();
    const url = new URL(req.url());
    const camino = url.pathname.replace(/^\/api/, "") + url.search;
    const hit = tabla.find(([m, re]) => m === req.method() && re.test(camino));
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      headers: { "X-Sesion-Restante": "1800" },
      body: JSON.stringify(hit ? hit[2] : {}),
    });
  });
  // El chat en vivo no tiene servidor en las pruebas: la vista muestra «Sin conexión en vivo».
  await page.routeWebSocket?.(/\/ws\//, (ws) => ws.close());
}

/** Inicia la sesión simulada guardando un token antes de cargar la app. */
export async function conSesion(page, usuario, extra = {}) {
  await simularApi(page, usuario);
  await page.addInitScript(({ datos }) => {
    sessionStorage.setItem("vl.token", "token-de-prueba");
    for (const [k, v] of Object.entries(datos)) sessionStorage.setItem(k, v);
  }, { datos: extra });
}

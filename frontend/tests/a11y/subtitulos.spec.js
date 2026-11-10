import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";
import { INSTRUCTOR, conSesion } from "./mocks.js";

/** RF-037 (HU-037): revisar, corregir y aprobar el borrador automático de subtítulos. */

const CURSO_VIDEO = {
  id: 5, titulo: "Metodologías ágiles", descripcion: "Curso", imagen: null, instructor: "Inés Gómez", estado: "BORRADOR",
  inscrito: false, porcentaje: 0, metodoPrincipal: null, metodoSecundario: null,
  modulos: [{ id: 3, titulo: "Fundamentos", descripcion: null, orden: 1, sinFormatoAfin: false, completo: false, evaluaciones: [],
    contenidos: [{ id: 1, titulo: "Clase en video", formato: "VIDEO", duracionMinutos: 5, publicado: false, afin: true,
      estado: null, porcentaje: null, conforme: false, faltantes: ["Falta cargar los subtítulos del video."] }] }],
};

const borrador = (estado, texto = "[Borrador automático de prueba] Hola") => ({
  contenidoId: 1, estado, origen: "AUTOMATICO", error: null, medioCambio: false, fechaGeneracion: "2026-11-10T10:00:00",
  fechaAprobacion: estado === "APROBADO" ? "2026-11-10T10:05:00" : null,
  lineas: [{ inicioMs: 0, finMs: 4000, texto }, { inicioMs: 4000, finMs: 9000, texto: "Segunda línea." }],
});

test("el instructor corrige y aprueba el borrador de subtítulos", async ({ page }) => {
  await conSesion(page, INSTRUCTOR);
  const json = (body) => ({ status: 200, contentType: "application/json", body: JSON.stringify(body) });
  let guardado = null;
  await page.route((u) => u.pathname === "/api/instructor/cursos/5", (r) => r.fulfill(json(CURSO_VIDEO)));
  await page.route((u) => u.pathname === "/api/contenidos/1", (r) => r.fulfill(json({
    id: 1, cursoId: 5, titulo: "Clase en video", descripcion: null, formato: "VIDEO", urlRecurso: "https://x/clase.mp4",
    cuerpo: null, duracionMinutos: 5, recursos: [], opcionesAlternativas: [], advertencias: [] })));
  await page.route((u) => u.pathname === "/api/contenidos/1/recursos", (r) => r.fulfill(json([])));
  await page.route((u) => u.pathname === "/api/contenidos/1/subtitulos/borrador", (r) => {
    if (r.request().method() === "PUT") {
      guardado = r.request().postDataJSON();
      return r.fulfill(json(borrador("LISTO", guardado.lineas[0].texto)));
    }
    return r.fulfill(json(borrador("LISTO")));
  });
  await page.route((u) => u.pathname === "/api/contenidos/1/subtitulos/borrador/aprobar", (r) =>
    r.fulfill(json(borrador("APROBADO", "Hola a todos, bienvenidos."))));

  await page.goto("/cursos/5/editar");
  await page.getByRole("button", { name: "Editar Clase en video" }).click();
  await expect(page.getByRole("heading", { name: "Subtítulos automáticos" })).toBeVisible();
  await expect(page.getByText(/Borrador listo para revisar/)).toBeVisible();

  const aprobar = page.getByRole("button", { name: "Aprobar subtítulos" });
  const texto = page.getByLabel("Texto de la línea 1");
  await texto.fill("Hola a todos, bienvenidos.");
  await expect(aprobar).toBeDisabled(); // hay cambios sin guardar
  await page.getByRole("button", { name: "Guardar subtítulos" }).click();
  await expect.poll(() => guardado?.lineas?.[0]).toEqual({ inicioMs: 0, finMs: 4000, texto: "Hola a todos, bienvenidos." });

  await aprobar.click();
  await expect(page.getByText("Subtítulos aprobados: ya forman parte del contenido.")).toBeVisible();

  const r = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"]).analyze();
  expect(r.violations.filter((v) => v.impact === "serious" || v.impact === "critical")).toEqual([]);
});

/** RF-039 (HU-039, criterio 4): al bloquearse la publicación se ofrece generar el borrador automático. */
test("si un podcast no tiene transcripción, se ofrece generar el borrador al publicar", async ({ page }) => {
  await conSesion(page, INSTRUCTOR);
  const json = (status, body) => ({ status, contentType: "application/json", body: JSON.stringify(body) });
  let pedido = false;
  await page.route((u) => u.pathname === "/api/instructor/cursos/5", (r) => r.fulfill(json(200, CURSO_VIDEO)));
  await page.route((u) => u.pathname === "/api/instructor/cursos/5/publicar", (r) => r.fulfill(json(422, {
    codigo: "CURSO_NO_CONFORME", mensaje: "No se puede publicar el curso: 1 contenido(s) tienen problemas de accesibilidad que hay que corregir.",
    hallazgos: [{ contenidoId: 8, contenido: "Podcast 1", regla: "AUDIO_SIN_TRANSCRIPCION", criterioWcag: "1.2.1",
      severidad: "BLOQUEANTE", mensaje: "El podcast no tiene transcripción.", comoCorregir: "Genera el borrador automático." }],
  })));
  await page.route((u) => u.pathname === "/api/contenidos/8/subtitulos/borrador", (r) => {
    pedido = r.request().method() === "POST";
    return r.fulfill(json(202, { contenidoId: 8, estado: "PENDIENTE", origen: "AUTOMATICO", lineas: [], medioCambio: false }));
  });

  await page.goto("/cursos/5/editar");
  await page.getByRole("button", { name: "Publicar curso" }).click();
  await page.getByRole("button", { name: "Sí, publicar" }).click();
  await page.getByRole("button", { name: "Cancelar" }).click();
  await expect(page.getByRole("heading", { name: "Qué corregir para publicar el curso" })).toBeVisible();
  await page.getByRole("button", { name: "Generar borrador automático de «Podcast 1»" }).click();
  await expect.poll(() => pedido).toBe(true);
  await expect(page.getByText(/Pedimos el borrador/)).toBeVisible();
});

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

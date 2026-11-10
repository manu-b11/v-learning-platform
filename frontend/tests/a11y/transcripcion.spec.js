import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";
import { ESTUDIANTE, conSesion } from "./mocks.js";

/** RF-038 (HU-038): transcripción sincronizada junto al video. */

const VTT = `WEBVTT

1
00:00:00.000 --> 00:00:04.000
Bienvenidos al curso.

2
00:00:04.000 --> 00:00:09.500
<v Inés>Hoy veremos los roles de Scrum.

3
00:00:09.500 --> 00:00:15.000
El Product Owner prioriza el backlog.
`;

const VIDEO = {
  id: 2, cursoId: 5, titulo: "Roles en video", descripcion: "Video corto.", formato: "VIDEO",
  urlRecurso: "/video.mp4", cuerpo: null, duracionMinutos: 1,
  recursos: [{ id: 3, tipo: "SUBTITULO", url: "/sub.vtt", descripcion: null, disponible: true },
    { id: 4, tipo: "TRANSCRIPCION", url: null, descripcion: "Bienvenidos al curso...", disponible: true }],
  opcionesAlternativas: [], lenguaSenasDisponible: false, mensajeLenguaSenas: null, apoyoCognitivo: false,
  advertencias: [], estado: "EN_PROGRESO", porcentaje: 0, tiempoConsumido: 0,
};

async function preparar(page, recursos = VIDEO.recursos) {
  await conSesion(page, ESTUDIANTE);
  await page.route((u) => u.pathname === "/api/contenidos/2", (r) =>
    r.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify({ ...VIDEO, recursos }) }));
  await page.route((u) => u.pathname === "/sub.vtt", (r) => r.fulfill({ status: 200, contentType: "text/vtt", body: VTT }));
  await page.route((u) => u.pathname === "/video.mp4", (r) => r.fulfill({ status: 404, body: "" }));
}

test("muestra la transcripción, salta al momento de la línea y se descarga", async ({ page }) => {
  await preparar(page);
  await page.goto("/contenidos/2");
  // El perfil simulado tiene «Transcripciones» activadas: el panel empieza abierto.
  const ocultar = page.getByRole("button", { name: "Ocultar transcripción" });
  await expect(ocultar).toHaveAttribute("aria-expanded", "true");
  await ocultar.click();
  const mostrar = page.getByRole("button", { name: "Mostrar transcripción" });
  await expect(mostrar).toHaveAttribute("aria-expanded", "false");
  await mostrar.click();
  const linea = page.getByRole("button", { name: /00:04 Hoy veremos los roles de Scrum\./ });
  await expect(linea).toBeVisible();

  // Operable con teclado: Tab hasta la línea y Enter.
  await linea.focus();
  const inicio = Date.now();
  await page.keyboard.press("Enter");
  await expect.poll(() => page.locator("video").evaluate((v) => v.currentTime)).toBe(4);
  expect(Date.now() - inicio).toBeLessThan(500 + 1000); // margen del sondeo de la prueba
  await expect(linea).toHaveAttribute("aria-current", "true");

  const descarga = page.waitForEvent("download");
  await page.getByRole("button", { name: /\.srt/ }).click();
  expect((await descarga).suggestedFilename()).toBe("Roles en video.srt");

  const r = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"]).analyze();
  expect(r.violations.filter((v) => v.impact === "serious" || v.impact === "critical")).toEqual([]);
});

test("sin subtítulos aprobados no se ofrece la transcripción sincronizada", async ({ page }) => {
  await preparar(page, [VIDEO.recursos[1]]);
  await page.goto("/contenidos/2");
  await expect(page.getByRole("heading", { name: "Roles en video" })).toBeVisible();
  await expect(page.getByRole("button", { name: /(Mostrar|Ocultar) transcripción/ })).toHaveCount(0);
});

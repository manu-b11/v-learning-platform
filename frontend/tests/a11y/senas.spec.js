import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";
import { ESTUDIANTE, conSesion } from "./mocks.js";

/** RF-040 (HU-040): intérprete de lengua de señas al lado del video y sincronizado. */

const VIDEO = {
  id: 3, cursoId: 5, titulo: "Clase en video", descripcion: null, formato: "VIDEO", urlRecurso: "/video.mp4",
  cuerpo: null, duracionMinutos: 2,
  recursos: [{ id: 7, tipo: "LENGUA_SENAS", url: "/interprete.mp4", descripcion: null, disponible: true, desfaseMs: 1500 }],
  opcionesAlternativas: [], lenguaSenasDisponible: true, mensajeLenguaSenas: null, apoyoCognitivo: false,
  advertencias: [], estado: "EN_PROGRESO", porcentaje: 0, tiempoConsumido: 0,
};

test("el intérprete aparece al lado y sigue los saltos del video con su desfase", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.route((u) => u.pathname === "/api/contenidos/3", (r) =>
    r.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify(VIDEO) }));
  await page.route((u) => u.pathname.endsWith(".mp4"), (r) => r.fulfill({ status: 404, body: "" }));
  await page.goto("/contenidos/3");

  await expect(page.getByText("Este contenido incluye interpretación en lengua de señas.")).toBeVisible();
  const boton = page.getByRole("button", { name: "Mostrar intérprete de lengua de señas" });
  await boton.click();
  await expect(page.getByRole("button", { name: "Ocultar intérprete de lengua de señas" })).toHaveAttribute("aria-pressed", "true");
  const interprete = page.getByLabel("Intérprete de lengua de señas");
  await expect(interprete).toBeAttached();

  // Salto en el video principal: el intérprete va al mismo momento + 1,5 s de desfase.
  await page.locator("video").first().evaluate((v) => { v.currentTime = 10; v.dispatchEvent(new Event("seeked")); });
  await expect.poll(() => interprete.evaluate((v) => v.currentTime)).toBeCloseTo(11.5, 1);

  const r = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"]).analyze();
  expect(r.violations.filter((v) => v.impact === "serious" || v.impact === "critical")).toEqual([]);
});

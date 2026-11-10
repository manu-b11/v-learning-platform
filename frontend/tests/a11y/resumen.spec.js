import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";
import { ESTUDIANTE, conSesion } from "./mocks.js";

/** RF-047 (HU-047): resumen diario y pausa de la gamificación. */

const LOGROS_EN_PAUSA = {
  puntos: 120, nivel: 2, puntosSiguienteNivel: 200, progresoNivelPorcentaje: 60, mostrarEnRanking: true,
  insignias: [], pendientes: [], avisosPausados: true,
};

test("con la gamificación en pausa no se muestran puntos ni ranking", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.route((u) => u.pathname === "/api/logros", (r) =>
    r.fulfill({ status: 200, contentType: "application/json", body: JSON.stringify(LOGROS_EN_PAUSA) }));
  await page.goto("/logros");
  await expect(page.getByRole("switch", { name: "Pausar la gamificación" })).toHaveAttribute("aria-checked", "true");
  await expect(page.getByText(/La gamificación está en pausa/)).toBeVisible();
  await expect(page.getByRole("heading", { name: "Clasificación del curso" })).toHaveCount(0);
  await expect(page.getByRole("link", { name: /puntos\. Ver mis logros/ })).toHaveCount(0);
});

test("el resumen diario se activa con su hora y la página es accesible", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.route((u) => u.pathname === "/api/notificaciones/preferencias", (r) =>
    r.fulfill({ status: 200, contentType: "application/json",
      body: JSON.stringify({ habilitadas: true, canal: "PLATAFORMA", resumenDiario: false, horaResumen: "18:00:00" }) }));
  await page.goto("/configuracion");
  const resumen = page.getByRole("switch", { name: "Resumen diario" });
  await resumen.click();
  await expect(page.getByLabel("Hora del resumen")).toHaveValue("18:00");
  const r = await new AxeBuilder({ page }).withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"]).analyze();
  expect(r.violations.filter((v) => v.impact === "serious" || v.impact === "critical")).toEqual([]);
});

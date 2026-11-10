import { test, expect } from "@playwright/test";
import AxeBuilder from "@axe-core/playwright";
import { ESTUDIANTE, INSTRUCTOR, conSesion, simularApi } from "./mocks.js";

/** Normas revisadas: WCAG 2.0 A/AA, 2.1 AA y 2.2 AA. Fallan solo las violaciones serias o críticas. */
const ETIQUETAS = ["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"];

async function revisar(page, nombre) {
  const r = await new AxeBuilder({ page }).withTags(ETIQUETAS).analyze();
  const graves = r.violations.filter((v) => v.impact === "serious" || v.impact === "critical");
  const resumen = graves.map((v) => `${v.id} (${v.impact}): ${v.help}\n  ${v.nodes.slice(0, 3).map((n) => n.target.join(" ")).join("\n  ")}`);
  expect(graves, `${nombre}: violaciones serias o críticas\n${resumen.join("\n")}`).toEqual([]);
}

test("inicio de sesión", async ({ page }) => {
  await simularApi(page, null);
  await page.goto("/login");
  await expect(page.getByRole("heading", { name: "Inicia sesión" })).toBeVisible();
  await revisar(page, "inicio de sesión");
});

test("verificación OTP (RF-044)", async ({ page }) => {
  await simularApi(page, null);
  await page.goto("/login");
  await page.getByLabel("Correo institucional").fill("sofia@tdea.edu.co");
  await page.getByLabel("Contraseña", { exact: true }).fill("Clave-segura-123");
  await page.getByRole("button", { name: "Continuar" }).click();
  const campo = page.getByLabel("Código de 6 dígitos");
  await expect(campo).toBeVisible();
  await expect(campo).toHaveAttribute("autocomplete", "one-time-code");
  await expect(campo).toHaveAttribute("inputmode", "numeric");
  await expect(page.getByRole("button", { name: "Reenviar código" })).toBeVisible();
  await revisar(page, "verificación OTP");
});

test("inicio del estudiante", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.goto("/inicio");
  await expect(page.getByRole("button", { name: "Ayuda" })).toBeVisible();
  await revisar(page, "inicio del estudiante");
});

test("lección", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.goto("/contenidos/1");
  await expect(page.getByRole("heading", { name: "Roles de Scrum" })).toBeVisible();
  await revisar(page, "lección");
});

test("tomar evaluación", async ({ page }) => {
  await conSesion(page, ESTUDIANTE, { "vl.eval.9": "7" });
  await page.goto("/intentos/9");
  await expect(page.getByRole("heading", { name: "¿Quién prioriza el backlog?" })).toBeVisible();
  await revisar(page, "tomar evaluación");
});

test("mi progreso", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.goto("/progreso");
  await expect(page.getByRole("heading", { name: /progreso/i, level: 1 })).toBeVisible();
  await revisar(page, "mi progreso");
});

test("comunidad", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  await page.goto("/comunidad/5");
  await expect(page.getByRole("log", { name: "Mensajes del curso" })).toBeVisible();
  await revisar(page, "comunidad");
});

test("editor de contenido del instructor (RF-049)", async ({ page }) => {
  await conSesion(page, INSTRUCTOR);
  await page.goto("/cursos/5/editar");
  await page.getByRole("button", { name: "Editar Roles de Scrum" }).click();
  await expect(page.getByRole("heading", { name: "Revisión de accesibilidad (WCAG 2.2)" })).toBeVisible();
  await expect(page.getByText("Bloquea la publicación:")).toBeVisible();
  await revisar(page, "editor de contenido");
});

test("ayuda consistente (RF-048)", async ({ page }) => {
  await conSesion(page, ESTUDIANTE);
  for (const ruta of ["/inicio", "/progreso", "/contenidos/1"]) {
    await page.goto(ruta);
    const boton = page.getByRole("banner").getByRole("button").first();
    await expect(boton).toHaveAccessibleName("Ayuda");
  }
  await page.getByRole("button", { name: "Ayuda" }).click();
  await expect(page.getByRole("button", { name: "Preguntar al Tutor de IA" })).toBeVisible();
  await revisar(page, "panel de ayuda");
});

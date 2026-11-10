import { test, expect } from "@playwright/test";
import { ESTUDIANTE, conSesion } from "./mocks.js";

/** RF-034 (HU-034): anunciar mensajes sin mover el foco, pausar y reanudar con contador. */

const mensaje = (id, contenido) => JSON.stringify({
  tipo: "MENSAJE",
  mensaje: {
    id, cursoId: 5, usuarioId: 10, autor: "Inés Gómez", rol: "INSTRUCTOR", contenido,
    fechaEnvio: "2026-11-10T10:05:00", estado: "ENVIADO", textoPlano: `Inés Gómez: ${contenido}`,
  },
});

test("anuncia el mensaje nuevo sin mover el foco y permite pausar", async ({ page }) => {
  let servidor;
  await conSesion(page, ESTUDIANTE, {}, { websocket: (ws) => { servidor = ws; } });
  await page.goto("/comunidad/5");
  const campo = page.getByLabel("Escribe un mensaje");
  await campo.focus();
  await expect.poll(() => Boolean(servidor)).toBe(true);

  servidor.send(mensaje(100, "Mañana hay quiz."));
  await expect(page.locator("[aria-live=polite]").getByText("Inés Gómez: Mañana hay quiz.")).toBeAttached();
  await expect(campo).toBeFocused();

  await page.getByRole("button", { name: "Pausar actualizaciones" }).click();
  servidor.send(mensaje(101, "Traigan calculadora."));
  await expect(page.getByText("En pausa: 1 mensaje(s) nuevo(s) sin mostrar.")).toBeVisible();
  const log = page.getByRole("log", { name: "Mensajes del curso" });
  await expect(log.getByText("Traigan calculadora.")).toHaveCount(0);

  await page.getByRole("button", { name: "Reanudar actualizaciones" }).click();
  await expect(log.getByText("Traigan calculadora.")).toBeVisible();
  await expect(page.locator("[aria-live=polite]").getByText("Se mostraron 1 mensaje(s) que llegaron en pausa.")).toBeAttached();
});

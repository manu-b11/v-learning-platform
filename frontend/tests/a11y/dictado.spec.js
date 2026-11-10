import { test, expect } from "@playwright/test";
import { ESTUDIANTE, conSesion } from "./mocks.js";

/** RF-045 (HU-045): dictar por voz en el chat; sin soporte se oculta el botón y se explica. */

test("el dictado agrega la frase al campo y se puede corregir", async ({ page }) => {
  await page.addInitScript(() => {
    // Reconocimiento de voz simulado: al iniciar devuelve una frase final.
    class Falso {
      start() {
        setTimeout(() => {
          this.onresult?.({ resultIndex: 0, results: [Object.assign([{ transcript: "hola a todos" }], { isFinal: true })] });
          this.onend?.();
        }, 50);
      }
      stop() { this.onend?.(); }
      abort() {}
    }
    window.SpeechRecognition = Falso;
  });
  await conSesion(page, ESTUDIANTE);
  await page.goto("/comunidad/5");
  const campo = page.getByLabel("Escribe un mensaje");
  await campo.fill("Buenos días,");
  await page.getByRole("button", { name: "Dictar en el mensaje" }).click();
  await expect(campo).toHaveValue("Buenos días, hola a todos");
  await campo.fill("Buenos días, hola a todas");
  await expect(campo).toHaveValue("Buenos días, hola a todas");
});

test("sin reconocimiento de voz el botón se oculta y se informa la causa", async ({ page }) => {
  await page.addInitScript(() => {
    delete window.SpeechRecognition;
    delete window.webkitSpeechRecognition;
  });
  await conSesion(page, ESTUDIANTE);
  await page.goto("/comunidad/5");
  await expect(page.getByLabel("Escribe un mensaje")).toBeVisible();
  await expect(page.getByRole("button", { name: /Dictar/ })).toHaveCount(0);
  await expect(page.getByText("Tu navegador no permite dictar por voz")).toBeVisible();
});

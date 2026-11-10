import { defineConfig } from "@playwright/test";

/**
 * Pruebas automáticas de accesibilidad (axe-core, WCAG 2.2 AA). El backend no hace falta: las
 * respuestas de la API se simulan con page.route (tests/a11y/mocks.js).
 * Navegador: el Chromium de Playwright (`npx playwright install chromium`) o, si ya tienes Chrome,
 * PW_CHANNEL=chrome npm run test:a11y.
 */
export default defineConfig({
  testDir: "./tests/a11y",
  timeout: 30_000,
  fullyParallel: true,
  reporter: [["list"]],
  use: {
    baseURL: "http://localhost:5174",
    channel: process.env.PW_CHANNEL || undefined,
    locale: "es-CO",
  },
  webServer: {
    command: "npx vite --port 5174 --strictPort",
    url: "http://localhost:5174",
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
});

import { defineConfig, devices } from "@playwright/test";

const FRONTEND_PORT = process.env.FRONTEND_PORT ?? "3000";
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";
const DEMO_USERNAME = process.env.NEXT_PUBLIC_DEMO_USERNAME ?? "demo";
const DEMO_PASSWORD = process.env.NEXT_PUBLIC_DEMO_PASSWORD ?? "demo1234";

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: `http://localhost:${FRONTEND_PORT}`,
    trace: "on-first-retry"
  },
  webServer: [
    {
      command: "cd .. && ./scripts/local/run-backend.sh",
      url: "http://localhost:8080/api/v1/public/ping",
      reuseExistingServer: !process.env.CI,
      timeout: 180_000
    },
    {
      command: `NEXT_PUBLIC_API_BASE_URL=${API_BASE_URL} NEXT_PUBLIC_DEMO_USERNAME=${DEMO_USERNAME} NEXT_PUBLIC_DEMO_PASSWORD=${DEMO_PASSWORD} npm run dev -- --hostname localhost --port ${FRONTEND_PORT}`,
      url: `http://localhost:${FRONTEND_PORT}`,
      cwd: ".",
      reuseExistingServer: !process.env.CI,
      timeout: 180_000
    }
  ],
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] }
    }
  ]
});

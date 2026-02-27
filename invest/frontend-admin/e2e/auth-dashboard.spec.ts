import { expect, test } from "@playwright/test";

async function ensureAuthenticated(page: import("@playwright/test").Page) {
  await page.goto("/");

  const dashboardTitle = page.getByText("Quant Portal Dashboard");
  const isDashboardVisible = await dashboardTitle.isVisible({ timeout: 15_000 }).catch(() => false);
  if (isDashboardVisible) {
    return;
  }

  const usernameInput = page.getByLabel("Username");
  const passwordInput = page.locator("input[name='password']");
  await expect(usernameInput).toBeVisible({ timeout: 30_000 });
  await expect(passwordInput).toBeVisible({ timeout: 30_000 });

  await usernameInput.fill(process.env.NEXT_PUBLIC_DEMO_USERNAME ?? "demo");
  await passwordInput.fill(process.env.NEXT_PUBLIC_DEMO_PASSWORD ?? "demo1234");
  await page.getByRole("button", { name: /sign in|log in/i }).click();

  await expect(dashboardTitle).toBeVisible({ timeout: 45_000 });
}

test("demo user can login and see dashboard widgets", async ({ page }) => {
  await ensureAuthenticated(page);

  await expect(page.getByText("Recent Transactions")).toBeVisible();
  await expect(page.getByText("Quick Start")).toBeVisible();
});

test("menu navigation to portfolios works", async ({ page }) => {
  await ensureAuthenticated(page);

  const portfoliosMenuItem = page.getByRole("menuitem", { name: "Portfolios" });
  await expect(portfoliosMenuItem).toBeVisible({ timeout: 30_000 });
  await portfoliosMenuItem.click();
  await expect(page).toHaveURL(/portfolios/);
});

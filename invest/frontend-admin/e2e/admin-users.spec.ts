import { expect, test } from "@playwright/test";

async function loginAs(page: import("@playwright/test").Page, username: string, password: string) {
  await page.goto("/");

  const usernameInput = page.getByLabel("Username");
  const passwordInput = page.locator("input[name='password']");
  await expect(usernameInput).toBeVisible({ timeout: 30_000 });
  await expect(passwordInput).toBeVisible({ timeout: 30_000 });

  await usernameInput.fill(username);
  await passwordInput.fill(password);
  await page.getByRole("button", { name: /sign in|log in/i }).click();
  await expect(page.getByText("Quant Portal Dashboard")).toBeVisible({ timeout: 45_000 });
}

test("admin can access users management page", async ({ page }) => {
  await loginAs(page, "admin", "admin1234");

  const usersMenuItem = page.getByRole("menuitem", { name: "Users" });
  await expect(usersMenuItem).toBeVisible({ timeout: 30_000 });
  await usersMenuItem.click();

  await expect(page).toHaveURL(/users/);
  await expect(page.getByRole("columnheader", { name: /username/i })).toBeVisible();
});

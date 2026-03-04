import { expect, test } from "@playwright/test";

test.describe("admin portal", () => {
    test.use({ baseURL: "http://127.0.0.1:3001" });

    test("dashboard, users, orders, config pages render backend data", async ({ page }) => {
        await page.goto("/dashboard");

        await expect(page.getByRole("heading", { name: "Scaffolding MSA" })).toBeVisible();
        await expect(page.getByRole("cell", { name: "Alice", exact: true })).toBeVisible();
        await expect(page.getByRole("cell", { name: "Bob", exact: true })).toBeVisible();

        await page.goto("/users");
        await expect(page.getByRole("heading", { name: "사용자 서비스 조회" })).toBeVisible();
        await expect(page.getByText("alice@example.com")).toBeVisible();
        await expect(page.getByText("bob@example.com")).toBeVisible();

        await page.goto("/orders");
        await expect(page.getByRole("heading", { name: "주문 서비스 조회" })).toBeVisible();
        await expect(page.getByText("ORD-SEED0001")).toBeVisible();

        await page.goto("/config");
        await expect(page.getByRole("heading", { name: "Config Server 원격 조회" })).toBeVisible();
        await expect(page.getByRole("button", { name: "Load Config" })).toBeVisible();
        await expect(page.locator(".config-source").first()).toBeVisible();
    });
});

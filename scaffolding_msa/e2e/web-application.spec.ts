import { expect, test } from "@playwright/test";

test.describe("web application", () => {
    test.use({ baseURL: "http://127.0.0.1:3002" });

    test("overview page shows aggregated runtime data", async ({ page }) => {
        await page.goto("/");

        await expect(page.getByRole("heading", { name: "고객용 웹 앱의 기본 표정까지 같이 세팅한다." })).toBeVisible();
        await expect(page.getByRole("link", { name: "Admin Portal" })).toBeVisible();
        await expect(page.getByText("alice@example.com")).toBeVisible();
        await expect(page.getByText("ORD-SEED0001")).toBeVisible();
        await expect(page.locator(".stack article").nth(0)).toContainText("2");
        await expect(page.locator(".stack article").nth(1)).toContainText("1");
    });
});

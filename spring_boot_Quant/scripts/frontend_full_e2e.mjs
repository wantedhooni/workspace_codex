import { chromium } from "playwright";
import fs from "node:fs";

const baseUrl = process.env.FRONTEND_URL || "http://127.0.0.1:5175";
const outDir = process.env.OUT_DIR || "/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts";
fs.mkdirSync(outDir, { recursive: true });

const checks = [
  { key: "orders", menu: "주문", route: "#/orders", api: "/api/orders" },
  { key: "trades", menu: "체결", route: "#/trades", api: "/api/trades" },
  { key: "positions", menu: "포지션", route: "#/positions", api: "/api/positions?portfolioId=1" },
  { key: "journal-vouchers", menu: "전표", route: "#/journalVouchers", api: "/api/journal-vouchers" },
  { key: "ledger-entries", menu: "원장", route: "#/ledgerEntries", api: "/api/ledgers/entries?portfolioId=1" }
];

const result = {
  startedAt: new Date().toISOString(),
  baseUrl,
  checks: []
};

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({ acceptDownloads: true });
const page = await context.newPage();

const demoUsername = process.env.DEMO_ADMIN_USERNAME || "admin@quant.io";
const demoPassword = process.env.DEMO_ADMIN_PASSWORD || "demo1234";

async function loginIfNeeded() {
  await page.goto(`${baseUrl}/#/login`, { waitUntil: "domcontentloaded" });
  const usernameInput = page.locator('input[name="username"]');
  const passwordInput = page.locator('input[name="password"]');
  const loginButton = page.getByRole("button", { name: /로그인|login|sign in/i });
  if (!(await usernameInput.count()) || !(await passwordInput.count()) || !(await loginButton.count())) {
    return;
  }
  await usernameInput.fill(demoUsername);
  await passwordInput.fill(demoPassword);
  await loginButton.first().click();
  await page.waitForFunction(() => !window.location.hash.startsWith("#/login"), { timeout: 10000 });
}

await loginIfNeeded();

for (const c of checks) {
  const item = { ...c, ok: false, apiStatus: null, rowCount: 0, exportDownloaded: false };

  await page.goto(`${baseUrl}/${c.route}`, { waitUntil: "domcontentloaded" });
  // 메뉴 클릭도 검증
  const menuNode = page.getByText(c.menu, { exact: true });
  if (await menuNode.count()) {
    await menuNode.first().click();
  }

  const apiResp = await page.waitForResponse(
    (r) => r.url().includes(c.api) && r.request().method() === "GET",
    { timeout: 15000 }
  );
  item.apiStatus = apiResp.status();
  if (apiResp.status() !== 200) {
    throw new Error(`${c.key}: API status ${apiResp.status()}`);
  }

  const tbodyRows = page.locator("tbody tr");
  await page.waitForTimeout(300);
  item.rowCount = await tbodyRows.count();
  if (item.rowCount < 1) {
    throw new Error(`${c.key}: no grid rows`);
  }

  const exportBtn = page.getByRole("button", { name: /export/i });
  if (await exportBtn.count()) {
    const [download] = await Promise.all([
      page.waitForEvent("download", { timeout: 10000 }).catch(() => null),
      exportBtn.first().click()
    ]);
    if (download) {
      const savePath = `${outDir}/frontend-${c.key}-export.csv`;
      await download.saveAs(savePath);
      item.exportDownloaded = true;
    }
  }

  await page.screenshot({ path: `${outDir}/frontend-${c.key}-full.png`, fullPage: true });
  item.ok = true;
  result.checks.push(item);
}

result.finishedAt = new Date().toISOString();
fs.writeFileSync(`${outDir}/frontend-full-e2e-result.json`, JSON.stringify(result, null, 2));

await context.close();
await browser.close();

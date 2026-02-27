import { chromium } from "playwright";
import fs from "node:fs";

const baseUrl = process.env.FRONTEND_URL || "http://127.0.0.1:5175";
const apiBase = process.env.API_BASE || "http://127.0.0.1:8088";
const outDir = process.env.OUT_DIR || "/Users/revy/workspace_codex/spring_boot_Quant/doc/test/artifacts";
fs.mkdirSync(outDir, { recursive: true });

const checks = [
  { key: "orders", menu: "주문", route: "#/orders", apiRequest: "/api/orders?portfolioId=1", expectCreate: true, expectExport: true },
  { key: "order-audits", menu: "주문감사", route: "#/orderAudits", apiRequest: "/api/orders/audit-logs?portfolioId=1", expectCreate: false, expectExport: true },
  { key: "trades", menu: "체결", route: "#/trades", apiRequest: "/api/trades?portfolioId=1", expectCreate: true, expectExport: true },
  { key: "positions", menu: "포지션", route: "#/positions", apiRequest: "/api/positions?portfolioId=1", expectCreate: false, expectExport: true },
  {
    key: "portfolio-summaries",
    menu: "포트폴리오 요약",
    route: "#/portfolioSummaries",
    apiRequest: "/api/portfolio-summaries?portfolioId=1",
    expectCreate: false,
    expectExport: true
  },
  {
    key: "order-health",
    menu: "주문건전성",
    route: "#/orderHealth",
    apiRequest: "/api/order-health?portfolioId=1&staleMinutes=0",
    expectCreate: false,
    expectExport: true
  },
  {
    key: "risk-alerts",
    menu: "리스크 경보",
    route: "#/riskAlerts",
    apiRequest: "/api/risk-alerts?portfolioId=1",
    expectCreate: false,
    expectExport: true
  },
  {
    key: "execution-qualities",
    menu: "체결품질",
    route: "#/executionQualities",
    apiRequest: "/api/execution-qualities?portfolioId=1",
    expectCreate: false,
    expectExport: true
  },
  {
    key: "risk-limits",
    menu: "리스크 한도",
    route: "#/riskLimits",
    apiRequest: "/api/risk-limits?portfolioId=1",
    expectCreate: true,
    expectExport: true
  },
  { key: "users", menu: "사용자", route: "#/users", apiRequest: "/api/users", expectCreate: true, expectExport: true },
  { key: "roles", menu: "권한", route: "#/roles", apiRequest: "/api/roles", expectCreate: true, expectExport: true },
  { key: "menus", menu: "메뉴", route: "#/menus", apiRequest: "/api/menus", expectCreate: true, expectExport: true },
  {
    key: "menu-permissions",
    menu: "메뉴권한",
    route: "#/menuPermissions",
    apiRequest: "/api/menu-permissions",
    expectCreate: true,
    expectExport: true
  },
  {
    key: "journal-vouchers",
    menu: "전표",
    route: "#/journalVouchers",
    apiRequest: "/api/journal-vouchers?portfolioId=1",
    expectCreate: true,
    expectExport: true
  },
  {
    key: "ledger-entries",
    menu: "원장",
    route: "#/ledgerEntries",
    apiRequest: "/api/ledgers/entries?portfolioId=1",
    expectCreate: false,
    expectExport: true
  },
  {
    key: "account-profile",
    menu: "내 계정",
    route: "#/accountProfile",
    apiRequest: "/api/account/me",
    expectCreate: false,
    expectExport: false
  },
  {
    key: "account-sessions",
    menu: "내 세션",
    route: "#/accountSessions",
    apiRequest: "/api/account/sessions",
    expectCreate: false,
    expectExport: true
  }
];

const result = {
  startedAt: new Date().toISOString(),
  baseUrl,
  apiBase,
  checks: [],
  actions: []
};

globalThis.__emergencyOrderIdForDashboard = null;

const demoUsername = process.env.DEMO_ADMIN_USERNAME || "admin@quant.io";
const demoPassword = process.env.DEMO_ADMIN_PASSWORD || "demo1234";

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({ acceptDownloads: true });
const page = await context.newPage();
const responseLogs = [];
let apiAuthHeaders = {};

page.on("response", (r) => {
  if (r.url().includes("/api/")) {
    responseLogs.push({
      url: r.url(),
      method: r.request().method(),
      status: r.status(),
      ts: Date.now()
    });
  }
});

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

function assertApiResponse({ since, method, apiPath, expectedStatus = 200, timeoutMs = 20000 }) {
  return new Promise(async (resolve, reject) => {
    const started = Date.now();
    while (Date.now() - started < timeoutMs) {
      const latest = responseLogs
        .filter((x) => x.ts >= since && x.method === method && x.url.includes(apiPath))
        .sort((a, b) => b.ts - a.ts)[0];

      if (latest) {
        if (latest.status !== expectedStatus) {
          reject(new Error(`${method} ${apiPath} status ${latest.status}`));
          return;
        }
        resolve(latest);
        return;
      }
      await sleep(200);
    }

    reject(new Error(`${method} ${apiPath} response not observed`));
  });
}

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

  await page.waitForFunction(
    () => {
      const hasSession = window.localStorage.getItem("quant.admin.session");
      return Boolean(hasSession) && !window.location.hash.startsWith("#/login");
    },
    { timeout: 10000 }
  );
}

async function createApiAuthHeaders() {
  const loginRes = await page.request.post(`${apiBase}/api/auth/login`, {
    data: { email: demoUsername, password: demoPassword }
  });
  if (!loginRes.ok()) {
    throw new Error(`api login failed: ${loginRes.status()}`);
  }

  const loginJson = await loginRes.json();
  if (!loginJson?.accessToken) {
    throw new Error("api login token missing");
  }

  apiAuthHeaders = {
    Authorization: `Bearer ${loginJson.accessToken}`
  };
}

async function checkCreateButton(routeKey) {
  const action = { key: `${routeKey}-create-button`, ok: false };
  const createBtn = page.getByRole("button", { name: /create|생성/i });
  const createLink = page.getByRole("link", { name: /create|생성/i });
  if (!(await createBtn.count()) && !(await createLink.count())) {
    action.reason = "create button not visible";
    result.actions.push(action);
    return false;
  }

  if (await createBtn.count()) {
    await createBtn.first().click();
  } else {
    await createLink.first().click();
  }
  await page.waitForTimeout(300);

  const dialog = page.getByRole("dialog");
  if (await dialog.count()) {
    const closeButton = dialog.first().getByRole("button", { name: /닫기|취소|cancel|close/i });
    if (await closeButton.count()) {
      await closeButton.first().click();
    } else {
      await page.keyboard.press("Escape");
    }
    await page.waitForTimeout(300);
  }

  action.ok = true;
  action.url = page.url();
  result.actions.push(action);
  return true;
}

async function runListChecks() {
  for (const c of checks) {
    const item = {
      ...c,
      ok: false,
      apiStatus: null,
      rowCount: 0,
      exportDownloaded: false,
      createButton: false,
      killSwitchToggle: false,
      tradingControlHistory: false,
      riskAlertAck: false,
      riskAlertWorkflow: false,
      riskAlertOverview: false,
      orderAuditSummary: false,
      portfolioInsight: false,
      staleRemediation: false,
      insightDialog: false
    };

    let targetUrl = `${baseUrl}/${c.route}?t=${Date.now()}`;
    if (c.key === "orders") {
      const orderFilter = encodeURIComponent(
        JSON.stringify({
          portfolioId: 1,
          symbol: "EMUI",
          status: "CANCELED"
        })
      );
      targetUrl = `${baseUrl}/${c.route}?filter=${orderFilter}&page=1&perPage=25&sort=orderId&order=DESC&t=${Date.now()}`;
    }
    await page.goto(targetUrl, { waitUntil: "domcontentloaded" });
    const loginButton = page.getByRole("button", { name: /로그인|login|sign in/i });
    if (await loginButton.count()) {
      await loginIfNeeded();
      await page.goto(targetUrl, { waitUntil: "domcontentloaded" });
    }

    const menuLink = page.getByRole("link", { name: c.menu, exact: true });
    if (await menuLink.count()) {
      await menuLink.first().click({ timeout: 3000 }).catch(() => {});
    }

    const apiRes = await page.request.get(`${apiBase}${c.apiRequest}`, { headers: apiAuthHeaders });
    item.apiStatus = apiRes.status();
    if (!apiRes.ok()) {
      throw new Error(`${c.key}: api status ${apiRes.status()}`);
    }

    await page.waitForTimeout(1200);
    const tbodyRows = page.locator("tbody tr");
    await tbodyRows.first().waitFor({ timeout: 10000 }).catch(() => {});
    item.rowCount = await tbodyRows.count();
    if (item.rowCount < 1) {
      const currentUrl = page.url();
      const bodyText = (await page.locator("body").innerText()).slice(0, 300).replace(/\s+/g, " ");
      throw new Error(`${c.key}: no grid rows (url=${currentUrl}, body=${bodyText})`);
    }

    if (c.key === "order-audits") {
      const actorColumn = page.locator("table thead").getByText("행위자");
      if (!(await actorColumn.count())) {
        throw new Error("order-audits: actor column missing");
      }

      const summaryHeading = page.getByText("감사 요약", { exact: false });
      if (!(await summaryHeading.count())) {
        throw new Error("order-audits: summary heading missing");
      }

      await assertApiResponse({
        since: Date.now() - 5000,
        method: "GET",
        apiPath: "/api/orders/audit-logs/summary",
        expectedStatus: 200
      });

      const quickActionChip = page.locator(".MuiChip-root", { hasText: /CREATE|TRADE_APPLIED|CANCEL|REJECT/ }).first();
      if (await quickActionChip.count()) {
        const quickFilterSince = Date.now();
        await quickActionChip.click();
        await assertApiResponse({
          since: quickFilterSince,
          method: "GET",
          apiPath: "/api/orders/audit-logs?",
          expectedStatus: 200
        });
      }

      item.orderAuditSummary = true;
    }

    if (c.key === "portfolio-summaries") {
      const insightHeading = page.getByText("포트폴리오 운영 인사이트", { exact: false });
      if (!(await insightHeading.count())) {
        throw new Error("portfolio-summaries: insight panel heading missing");
      }

      await assertApiResponse({
        since: Date.now() - 10000,
        method: "GET",
        apiPath: "/api/portfolio-summaries/insight",
        expectedStatus: 200
      });

      const healthChip = page.locator(".MuiChip-root", { hasText: /HEALTHY|WARN|CRITICAL/ }).first();
      if (!(await healthChip.count())) {
        throw new Error("portfolio-summaries: health status chip missing");
      }

      item.portfolioInsight = true;
    }

    if (c.key === "orders") {
      const insightButton = page.locator("tbody tr").first().getByRole("button", { name: "인사이트" });
      if (!(await insightButton.count())) {
        throw new Error("orders: insight button missing");
      }

      const insightSince = Date.now();
      await insightButton.first().click();
      await assertApiResponse({
        since: insightSince,
        method: "GET",
        apiPath: "/insight",
        expectedStatus: 200
      });

      const insightDialog = page.getByRole("dialog");
      await insightDialog.first().waitFor({ timeout: 10000 });
      const summaryTab = insightDialog.first().getByRole("tab", { name: /요약/i });
      if (!(await summaryTab.count())) {
        throw new Error("orders: insight dialog summary tab missing");
      }
      await page.screenshot({ path: `${outDir}/frontend-orders-insight-modal.png`, fullPage: true });
      await page.keyboard.press("Escape");
      await page.waitForTimeout(300);
      item.insightDialog = true;
    }

    if (c.key === "order-health") {
      const remediateBtn = page.getByRole("button", { name: "지연주문 일괄취소" });
      if (!(await remediateBtn.count())) {
        throw new Error("order-health: remediation button missing");
      }

      const remediateSince = Date.now();
      await remediateBtn.first().click();
      await assertApiResponse({
        since: remediateSince,
        method: "POST",
        apiPath: "/api/order-health/remediate-stale",
        expectedStatus: 200
      });

      item.staleRemediation = true;
    }

    if (c.key === "risk-limits") {
      const disableBtn = page.getByRole("button", { name: "거래중지" });
      if (!(await disableBtn.count())) {
        throw new Error("risk-limits: disable button missing");
      }

      const disableSince = Date.now();
      await disableBtn.first().click();
      await assertApiResponse({
        since: disableSince,
        method: "PUT",
        apiPath: "/api/risk-limits/trading-controls",
        expectedStatus: 200
      });

      await page.waitForTimeout(500);
      const enableBtn = page.getByRole("button", { name: "거래재개" });
      if (!(await enableBtn.count())) {
        throw new Error("risk-limits: enable button missing");
      }

      const enableSince = Date.now();
      await enableBtn.first().click();
      await assertApiResponse({
        since: enableSince,
        method: "PUT",
        apiPath: "/api/risk-limits/trading-controls",
        expectedStatus: 200
      });

      const historyBtn = page.getByRole("button", { name: "이력" });
      if (!(await historyBtn.count())) {
        throw new Error("risk-limits: history button missing");
      }
      const historySince = Date.now();
      await historyBtn.first().click();
      await assertApiResponse({
        since: historySince,
        method: "GET",
        apiPath: "/api/risk-limits/trading-controls/history",
        expectedStatus: 200
      });
      const historyDialog = page.getByRole("dialog", { name: "거래 통제 이력" });
      await historyDialog.first().waitFor({ timeout: 10000 });
      const historyActionColumn = historyDialog.first().getByRole("columnheader", { name: "액션" });
      if (!(await historyActionColumn.count())) {
        throw new Error("risk-limits: history dialog column missing");
      }
      await page.screenshot({ path: `${outDir}/frontend-risk-limits-history-modal.png`, fullPage: true });
      await page.keyboard.press("Escape");
      await page.waitForTimeout(300);
      item.tradingControlHistory = true;

      item.killSwitchToggle = true;
    }

    if (c.key === "risk-alerts") {
      const disableSetupRes = await page.request.put(`${apiBase}/api/risk-limits/trading-controls`, {
        headers: {
          ...apiAuthHeaders,
          "Content-Type": "application/json"
        },
        data: {
          portfolioId: 1,
          tradingEnabled: false,
          reason: "e2e alert ack setup"
        }
      });
      if (!disableSetupRes.ok()) {
        throw new Error(`risk-alerts: setup disable failed ${disableSetupRes.status()}`);
      }

      const riskAlertFilter = encodeURIComponent(
        JSON.stringify({
          portfolioId: 1
        })
      );
      await page.goto(
        `${baseUrl}/${c.route}?filter=${riskAlertFilter}&page=1&perPage=25&sort=occurredAt&order=DESC&t=${Date.now()}`,
        { waitUntil: "domcontentloaded" }
      );
      await page.waitForTimeout(700);

      const overviewHeading = page.getByText("리스크 운영 오버뷰", { exact: false });
      if (!(await overviewHeading.count())) {
        throw new Error("risk-alerts: overview panel heading missing");
      }
      await assertApiResponse({
        since: Date.now() - 10000,
        method: "GET",
        apiPath: "/api/risk-alerts/overview",
        expectedStatus: 200
      });
      item.riskAlertOverview = true;

      const slaFilterBtn = page.getByRole("button", { name: "SLA 위반만" });
      if (!(await slaFilterBtn.count())) {
        throw new Error("risk-alerts: sla filter button missing");
      }
      const slaFilterSince = Date.now();
      await slaFilterBtn.first().click();
      await assertApiResponse({
        since: slaFilterSince,
        method: "GET",
        apiPath: "/api/risk-alerts?",
        expectedStatus: 200
      });
      await page.waitForTimeout(250);
      const resetFilterBtn = page.getByRole("button", { name: "필터 초기화" });
      if (await resetFilterBtn.count()) {
        await resetFilterBtn.first().click();
        await page.waitForTimeout(250);
      }

      const findEnabledActionButton = async (label) => {
        const buttons = page.locator("button:not([disabled])");
        const count = await buttons.count();
        for (let i = 0; i < count; i += 1) {
          const text = ((await buttons.nth(i).textContent()) ?? "").trim();
          if (text === label) {
            return buttons.nth(i);
          }
        }
        return null;
      };

      let ackBtn = await findEnabledActionButton("확인");
      if (!ackBtn) {
        const alreadyAckedBtn = await findEnabledActionButton("확인해제");
        if (alreadyAckedBtn) {
          const normalizeSince = Date.now();
          await alreadyAckedBtn.click();
          await assertApiResponse({
            since: normalizeSince,
            method: "POST",
            apiPath: "/api/risk-alerts/unack",
            expectedStatus: 200
          });
          await page.waitForTimeout(300);
          ackBtn = await findEnabledActionButton("확인");
        }
      }
      if (!ackBtn) {
        throw new Error("risk-alerts: ack button missing");
      }

      const ackSince = Date.now();
      await ackBtn.click();
      await assertApiResponse({
        since: ackSince,
        method: "POST",
        apiPath: "/api/risk-alerts/ack",
        expectedStatus: 200
      });
      await page.waitForTimeout(400);

      const inProgressBtn = await findEnabledActionButton("진행중");
      if (!inProgressBtn) {
        throw new Error("risk-alerts: in-progress button missing");
      }
      const inProgressSince = Date.now();
      await inProgressBtn.click();
      await assertApiResponse({
        since: inProgressSince,
        method: "POST",
        apiPath: "/api/risk-alerts/workflow",
        expectedStatus: 200
      });
      await page.waitForTimeout(300);

      const resolvedBtn = await findEnabledActionButton("해결");
      if (!resolvedBtn) {
        throw new Error("risk-alerts: resolved button missing");
      }
      const resolvedSince = Date.now();
      await resolvedBtn.click();
      await assertApiResponse({
        since: resolvedSince,
        method: "POST",
        apiPath: "/api/risk-alerts/workflow",
        expectedStatus: 200
      });
      await page.waitForTimeout(300);

      const reopenBtn = await findEnabledActionButton("재오픈");
      if (!reopenBtn) {
        throw new Error("risk-alerts: reopen button missing");
      }
      const reopenSince = Date.now();
      await reopenBtn.click();
      await assertApiResponse({
        since: reopenSince,
        method: "POST",
        apiPath: "/api/risk-alerts/workflow",
        expectedStatus: 200
      });
      await page.waitForTimeout(300);

      await page.waitForTimeout(500);
      const unackBtn = await findEnabledActionButton("확인해제");
      if (!unackBtn) {
        throw new Error("risk-alerts: unack button missing");
      }

      const unackSince = Date.now();
      await unackBtn.click();
      await assertApiResponse({
        since: unackSince,
        method: "POST",
        apiPath: "/api/risk-alerts/unack",
        expectedStatus: 200
      });

      const enableRestoreRes = await page.request.put(`${apiBase}/api/risk-limits/trading-controls`, {
        headers: {
          ...apiAuthHeaders,
          "Content-Type": "application/json"
        },
        data: {
          portfolioId: 1,
          tradingEnabled: true,
          reason: "e2e alert ack restore"
        }
      });
      if (!enableRestoreRes.ok()) {
        throw new Error(`risk-alerts: restore enable failed ${enableRestoreRes.status()}`);
      }

      item.riskAlertAck = true;
      item.riskAlertWorkflow = true;
    }

    if (c.expectExport) {
      const exportBtn = page.getByRole("button", { name: /export/i });
      if (await exportBtn.count()) {
        const [download] = await Promise.all([
          page.waitForEvent("download", { timeout: 10000 }).catch(() => null),
          exportBtn.first().click()
        ]);
        if (download) {
          await download.saveAs(`${outDir}/frontend-${c.key}-export.csv`);
          item.exportDownloaded = true;
        }
      }
    }

    if (c.expectCreate) {
      const found = await checkCreateButton(c.key);
      if (!found) {
        throw new Error(`${c.key}: create button expected but not found`);
      }
      item.createButton = true;
    }

    await page.screenshot({ path: `${outDir}/frontend-${c.key}-full.png`, fullPage: true });
    item.ok = true;
    result.checks.push(item);
  }
}

async function runDashboardChecks() {
  const action = {
    key: "dashboard-work-queue",
    ok: false,
    apiStatus: null,
    activityApiStatus: null,
    usedApiFallback: false,
    taskCards: 0,
    quickAction: false,
    approvedVoucherQuickAction: false,
    draftVoucherQuickAction: false,
    emergencyRiskQuickAction: false,
    resumeTradingQuickAction: false,
    resumeTradingForced: false,
    emergencyCanceledOrder: false
  };
  await page.goto(`${baseUrl}/#/`, { waitUntil: "domcontentloaded" });

  const queueRes = await page.request.get(`${apiBase}/api/account/work-queue?portfolioId=1&staleMinutes=30&topN=6`, {
    headers: apiAuthHeaders
  });
  action.apiStatus = queueRes.status();
  if (!queueRes.ok()) {
    throw new Error(`dashboard-work-queue api status ${queueRes.status()}`);
  }

  const activityRes = await page.request.get(`${apiBase}/api/account/activity-feed?portfolioId=1&limit=6`, {
    headers: apiAuthHeaders
  });
  action.activityApiStatus = activityRes.status();
  if (!activityRes.ok()) {
    throw new Error(`dashboard-activity-feed api status ${activityRes.status()}`);
  }

  const heading = page.getByText("오늘의 운영 작업", { exact: false });
  const feedHeading = page.getByText("내 활동 피드", { exact: false });
  const waitDashboardHeading = async () => {
    await heading.first().waitFor({ timeout: 20000 });
    await feedHeading.first().waitFor({ timeout: 20000 });
  };

  const loginButton = page.getByRole("button", { name: /로그인|login|sign in/i });
  let uiAvailable = false;
  try {
    await waitDashboardHeading();
    uiAvailable = true;
  } catch (e) {
    if (await loginButton.count()) {
      await loginIfNeeded();
      await page.goto(`${baseUrl}/#/`, { waitUntil: "domcontentloaded" });
      await waitDashboardHeading().then(() => {
        uiAvailable = true;
      });
    } else {
      uiAvailable = false;
    }
  }

  if (uiAvailable) {
    const taskCards = page.locator("text=/경보 확인|정리 실행|전표 처리|승인 처리|주문 보기|세션 정리|상세 보기|리스크 점검/");
    action.taskCards = await taskCards.count();

    const criticalRiskCard = page.locator(".MuiCard-root", {
      has: page.getByText("치명 리스크 경보 확인", { exact: false })
    });
    if (!(await criticalRiskCard.count())) {
      throw new Error("dashboard: critical risk task card missing");
    }
    const criticalQuickAction = criticalRiskCard.first().getByRole("button", { name: "즉시 실행" });
    if (!(await criticalQuickAction.count())) {
      throw new Error("dashboard: critical risk quick action missing");
    }
    const criticalActionSince = Date.now();
    await criticalQuickAction.first().click();
    await assertApiResponse({
      since: criticalActionSince,
      method: "POST",
      apiPath: "/api/account/work-queue/actions/emergency-risk-response",
      expectedStatus: 200
    });
    action.emergencyRiskQuickAction = true;

    const resumeTradingCard = page.locator(".MuiCard-root", {
      has: page.getByText("거래 재개 검증", { exact: false })
    });
    if (await resumeTradingCard.count()) {
      const resumeQuickAction = resumeTradingCard.first().getByRole("button", { name: "즉시 실행" });
      if (!(await resumeQuickAction.count())) {
        throw new Error("dashboard: resume trading quick action missing");
      }
      const resumeActionSince = Date.now();
      await resumeQuickAction.first().click();
      await assertApiResponse({
        since: resumeActionSince,
        method: "POST",
        apiPath: "/api/account/work-queue/actions/resume-trading",
        expectedStatus: 200
      });
    } else {
      const resumeRes = await page.request.post(`${apiBase}/api/account/work-queue/actions/resume-trading`, {
        headers: {
          ...apiAuthHeaders,
          "Content-Type": "application/json"
        },
        data: {
          portfolioId: 1,
          reason: "dashboard fallback resume (card missing)",
          force: false
        }
      });
      if (!resumeRes.ok()) {
        throw new Error(`dashboard fallback resume failed ${resumeRes.status()}`);
      }
      action.usedApiFallback = true;
    }
    action.resumeTradingQuickAction = true;

    const approvedVoucherCard = page.locator(".MuiCard-root", {
      has: page.getByText("승인 전표 전기", { exact: false })
    });
    if (!(await approvedVoucherCard.count())) {
      throw new Error("dashboard: approved voucher task card missing");
    }
    const approveQuickAction = approvedVoucherCard.first().getByRole("button", { name: "즉시 실행" });
    if (!(await approveQuickAction.count())) {
      throw new Error("dashboard: approved voucher quick action missing");
    }
    const approvedActionSince = Date.now();
    await approveQuickAction.first().click();
    await assertApiResponse({
      since: approvedActionSince,
      method: "POST",
      apiPath: "/api/account/work-queue/actions/post-approved-vouchers",
      expectedStatus: 200
    });
    action.approvedVoucherQuickAction = true;

    const draftVoucherCard = page.locator(".MuiCard-root", {
      has: page.getByText("초안 전표 승인", { exact: false })
    });
    if (!(await draftVoucherCard.count())) {
      throw new Error("dashboard: draft voucher task card missing");
    }
    const draftQuickAction = draftVoucherCard.first().getByRole("button", { name: "즉시 실행" });
    if (!(await draftQuickAction.count())) {
      throw new Error("dashboard: draft voucher quick action missing");
    }
    const draftActionSince = Date.now();
    await draftQuickAction.first().click();
    await assertApiResponse({
      since: draftActionSince,
      method: "POST",
      apiPath: "/api/account/work-queue/actions/approve-draft-vouchers",
      expectedStatus: 200
    });
    action.draftVoucherQuickAction = true;
  } else {
    const emergencyRes = await page.request.post(`${apiBase}/api/account/work-queue/actions/emergency-risk-response`, {
      headers: {
        ...apiAuthHeaders,
        "Content-Type": "application/json"
      },
      data: {
        portfolioId: 1,
        reason: "dashboard fallback emergency",
        cancelOpenOrders: true
      }
    });
    if (!emergencyRes.ok()) {
      throw new Error(`dashboard fallback emergency failed ${emergencyRes.status()}`);
    }
    action.emergencyRiskQuickAction = true;

    const resumeRes = await page.request.post(`${apiBase}/api/account/work-queue/actions/resume-trading`, {
      headers: {
        ...apiAuthHeaders,
        "Content-Type": "application/json"
      },
      data: {
        portfolioId: 1,
        reason: "dashboard fallback resume",
        force: false
      }
    });
    if (!resumeRes.ok()) {
      throw new Error(`dashboard fallback resume failed ${resumeRes.status()}`);
    }
    action.resumeTradingQuickAction = true;

    const approvedRes = await page.request.post(`${apiBase}/api/account/work-queue/actions/post-approved-vouchers`, {
      headers: {
        ...apiAuthHeaders,
        "Content-Type": "application/json"
      },
      data: {
        portfolioId: 1,
        limit: 20,
        reason: "dashboard fallback approved voucher"
      }
    });
    if (!approvedRes.ok()) {
      throw new Error(`dashboard fallback approved vouchers failed ${approvedRes.status()}`);
    }
    action.approvedVoucherQuickAction = true;

    const draftRes = await page.request.post(`${apiBase}/api/account/work-queue/actions/approve-draft-vouchers`, {
      headers: {
        ...apiAuthHeaders,
        "Content-Type": "application/json"
      },
      data: {
        portfolioId: 1,
        limit: 20,
        reason: "dashboard fallback draft voucher"
      }
    });
    if (!draftRes.ok()) {
      throw new Error(`dashboard fallback draft vouchers failed ${draftRes.status()}`);
    }
    action.draftVoucherQuickAction = true;
    action.usedApiFallback = true;
  }

  if (globalThis.__emergencyOrderIdForDashboard) {
    const ordersAfterRes = await page.request.get(
      `${apiBase}/api/orders?portfolioId=1&symbol=${encodeURIComponent("EMUI")}`,
      { headers: apiAuthHeaders }
    );
    if (!ordersAfterRes.ok()) {
      throw new Error(`dashboard: emergency order check failed ${ordersAfterRes.status()}`);
    }
    const ordersAfterJson = await ordersAfterRes.json();
    const canceled = Array.isArray(ordersAfterJson.items)
      && ordersAfterJson.items.some((it) => Number(it.orderId) === Number(globalThis.__emergencyOrderIdForDashboard) && String(it.status) === "CANCELED");
    if (!canceled) {
      throw new Error("dashboard: emergency response did not cancel open order");
    }
    action.emergencyCanceledOrder = true;
  }

  action.quickAction =
    action.approvedVoucherQuickAction
    || action.draftVoucherQuickAction
    || action.emergencyRiskQuickAction
    || action.resumeTradingQuickAction;

  const tradingControlRes = await page.request.get(`${apiBase}/api/risk-limits/trading-controls?portfolioId=1`, {
    headers: apiAuthHeaders
  });
  if (!tradingControlRes.ok()) {
    throw new Error(`dashboard: trading control fetch failed ${tradingControlRes.status()}`);
  }
  const tradingControlJson = await tradingControlRes.json();
  const tradingEnabled = Boolean(tradingControlJson?.items?.[0]?.tradingEnabled);
  if (!tradingEnabled) {
    const forcedResumeRes = await page.request.post(`${apiBase}/api/account/work-queue/actions/resume-trading`, {
      headers: {
        ...apiAuthHeaders,
        "Content-Type": "application/json"
      },
      data: {
        portfolioId: 1,
        reason: "resume after dashboard e2e",
        force: true
      }
    });
    if (!forcedResumeRes.ok()) {
      throw new Error(`dashboard: forced resume trading failed ${forcedResumeRes.status()}`);
    }
    action.resumeTradingForced = true;
  }

  await page.screenshot({ path: `${outDir}/frontend-dashboard-work-queue.png`, fullPage: true });

  action.ok = true;
  result.actions.push(action);
}

async function seedTradeVoucherFlow() {
  const symbol = `E2E${Date.now().toString().slice(-4)}`;

  const orderRes = await page.request.post(`${apiBase}/api/orders`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      symbol,
      side: "BUY",
      quantity: 7
    }
  });
  if (!orderRes.ok()) {
    throw new Error(`seed order failed: ${orderRes.status()}`);
  }
  const order = await orderRes.json();

  const tradeRes = await page.request.post(`${apiBase}/api/trades/events`, {
    headers: apiAuthHeaders,
    data: {
      orderId: order.orderId,
      tradeQuantity: 7,
      tradePrice: 150.25
    }
  });
  if (!tradeRes.ok()) {
    throw new Error(`seed trade failed: ${tradeRes.status()}`);
  }
  const trade = await tradeRes.json();

  const voucherRes = await page.request.post(`${apiBase}/api/journal-vouchers`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      tradeId: trade.tradeId,
      description: `e2e voucher ${symbol}`,
      entries: [
        { accountCode: "STOCK_ASSET", drCr: "DR", amount: 1051.75, symbol, description: "asset increase" },
        { accountCode: "CASH", drCr: "CR", amount: 1051.75, description: "cash out" }
      ]
    }
  });
  if (!voucherRes.ok()) {
    throw new Error(`seed voucher failed: ${voucherRes.status()}`);
  }
  const voucher = await voucherRes.json();

  result.actions.push({ key: "seed", ok: true, orderId: order.orderId, tradeId: trade.tradeId, voucherId: voucher.voucherId, symbol });
  return { order, trade, voucher, symbol };
}

async function seedApprovedVoucherForDashboard() {
  const symbol = `AQV${Date.now().toString().slice(-4)}`;
  const voucherRes = await page.request.post(`${apiBase}/api/journal-vouchers`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      tradeId: null,
      description: `dashboard approved voucher ${symbol}`,
      entries: [
        { accountCode: "STOCK_ASSET", drCr: "DR", amount: 1000.0, symbol, description: "approved voucher dr" },
        { accountCode: "CASH", drCr: "CR", amount: 1000.0, description: "approved voucher cr" }
      ]
    }
  });
  if (!voucherRes.ok()) {
    throw new Error(`seed approved voucher failed: ${voucherRes.status()}`);
  }
  const voucher = await voucherRes.json();

  const approveRes = await page.request.post(`${apiBase}/api/journal-vouchers/${voucher.voucherId}/approve`, {
    headers: apiAuthHeaders,
    data: {}
  });
  if (!approveRes.ok()) {
    throw new Error(`seed approved voucher approve failed: ${approveRes.status()}`);
  }

  result.actions.push({ key: "seed-approved-voucher", ok: true, voucherId: voucher.voucherId, symbol });
  return voucher;
}

async function seedDraftVoucherForDashboard() {
  const symbol = `DQV${Date.now().toString().slice(-4)}`;
  const voucherRes = await page.request.post(`${apiBase}/api/journal-vouchers`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      tradeId: null,
      description: `dashboard draft voucher ${symbol}`,
      entries: [
        { accountCode: "STOCK_ASSET", drCr: "DR", amount: 900.0, symbol, description: "draft voucher dr" },
        { accountCode: "CASH", drCr: "CR", amount: 900.0, description: "draft voucher cr" }
      ]
    }
  });
  if (!voucherRes.ok()) {
    throw new Error(`seed draft voucher failed: ${voucherRes.status()}`);
  }
  const voucher = await voucherRes.json();
  result.actions.push({ key: "seed-draft-voucher", ok: true, voucherId: voucher.voucherId, symbol });
  return voucher;
}

async function seedCriticalRiskForDashboard() {
  const riskRes = await page.request.get(`${apiBase}/api/risk-limits?portfolioId=1`, {
    headers: apiAuthHeaders
  });
  if (!riskRes.ok()) {
    throw new Error(`seed critical risk fetch failed: ${riskRes.status()}`);
  }
  const riskJson = await riskRes.json();
  const current = Array.isArray(riskJson.items) && riskJson.items.length > 0 ? riskJson.items[0] : {};

  const upsertRes = await page.request.put(`${apiBase}/api/risk-limits`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      maxOrderNotional: Number(current.maxOrderNotional ?? 2_000_000),
      maxPositionNotionalPerSymbol: Number(current.maxPositionNotionalPerSymbol ?? 5_000_000),
      maxDailyTurnover: Number(current.maxDailyTurnover ?? 8_000_000),
      maxOpenOrdersPerSymbol: 1,
      commissionBps: Number(current.commissionBps ?? 2),
      slippageBps: Number(current.slippageBps ?? 1)
    }
  });
  if (!upsertRes.ok()) {
    throw new Error(`seed critical risk failed: ${upsertRes.status()}`);
  }
  result.actions.push({ key: "seed-critical-risk", ok: true, portfolioId: 1, mode: "maxOpenOrdersPerSymbol=1" });
}

async function seedEmergencyOpenOrderForDashboard() {
  const orderRes = await page.request.post(`${apiBase}/api/orders`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      symbol: "EMUI",
      side: "BUY",
      orderType: "LIMIT",
      timeInForce: "DAY",
      limitPrice: 115.25,
      quantity: 2
    }
  });
  if (!orderRes.ok()) {
    throw new Error(`seed emergency open order failed: ${orderRes.status()}`);
  }
  const order = await orderRes.json();
  globalThis.__emergencyOrderIdForDashboard = Number(order.orderId);
  result.actions.push({ key: "seed-emergency-open-order", ok: true, orderId: order.orderId, symbol: "EMUI" });
}

async function runVoucherActionFlow(voucherId) {
  const actionResult = {
    key: "voucher-actions",
    voucherId,
    targetVoucherId: voucherId,
    usedUiButtons: false,
    usedApiFallback: false,
    approve: false,
    post: false,
    ledgerVisible: false,
    ledgerEntries: 0
  };

  const voucherFilter = encodeURIComponent(
    JSON.stringify({
      portfolioId: 1,
      status: "DRAFT"
    })
  );
  await page.goto(
    `${baseUrl}/#/journalVouchers?filter=${voucherFilter}&page=1&perPage=25&sort=voucherId&order=DESC&t=${Date.now()}`,
    { waitUntil: "domcontentloaded" }
  );

  const voucherRow = page.locator("tbody tr").first();
  let targetVoucherId = Number(voucherId);
  const hasRow = await voucherRow
    .waitFor({ timeout: 8000 })
    .then(() => true)
    .catch(() => false);

  if (hasRow) {
    const firstVoucherIdCellText = await voucherRow.locator("td").first().innerText();
    const parsedVoucherId = Number(firstVoucherIdCellText.replace(/[^0-9]/g, ""));
    targetVoucherId = Number.isFinite(parsedVoucherId) && parsedVoucherId > 0 ? parsedVoucherId : Number(voucherId);
  }
  actionResult.targetVoucherId = targetVoucherId;

  if (hasRow) {
    const approveAt = Date.now();
    await voucherRow.first().getByRole("button", { name: "승인" }).click();
    await assertApiResponse({ since: approveAt, method: "POST", apiPath: `/api/journal-vouchers/${targetVoucherId}/approve` });
    actionResult.approve = true;

    const postButton = voucherRow.first().getByRole("button", { name: "전기" });
    for (let i = 0; i < 20; i += 1) {
      if (!(await postButton.isDisabled())) {
        break;
      }
      await page.waitForTimeout(200);
    }

    if (await postButton.isDisabled()) {
      const postFallbackRes = await page.request.post(`${apiBase}/api/journal-vouchers/${targetVoucherId}/post`, {
        headers: apiAuthHeaders,
        data: {}
      });
      if (!postFallbackRes.ok()) {
        throw new Error(`voucher post after approve failed: ${postFallbackRes.status()}`);
      }
      actionResult.post = true;
      actionResult.usedApiFallback = true;
    } else {
      const postAt = Date.now();
      await postButton.click();
      await assertApiResponse({ since: postAt, method: "POST", apiPath: `/api/journal-vouchers/${targetVoucherId}/post` });
      actionResult.post = true;
    }
    actionResult.usedUiButtons = true;
  } else {
    const approveRes = await page.request.post(`${apiBase}/api/journal-vouchers/${targetVoucherId}/approve`, {
      headers: apiAuthHeaders,
      data: {}
    });
    if (!approveRes.ok()) {
      throw new Error(`voucher approve fallback failed: ${approveRes.status()}`);
    }
    actionResult.approve = true;

    const postRes = await page.request.post(`${apiBase}/api/journal-vouchers/${targetVoucherId}/post`, {
      headers: apiAuthHeaders,
      data: {}
    });
    if (!postRes.ok()) {
      throw new Error(`voucher post fallback failed: ${postRes.status()}`);
    }
    actionResult.post = true;
    actionResult.usedApiFallback = true;
  }

  const ledgerRes = await page.request.get(`${apiBase}/api/ledgers/entries?portfolioId=1&voucherId=${targetVoucherId}`, {
    headers: apiAuthHeaders
  });
  if (!ledgerRes.ok()) {
    throw new Error(`ledger api failed for voucher ${targetVoucherId}: ${ledgerRes.status()}`);
  }
  const ledgerJson = await ledgerRes.json();
  actionResult.ledgerEntries = Array.isArray(ledgerJson.items) ? ledgerJson.items.length : 0;
  actionResult.ledgerVisible = actionResult.ledgerEntries > 0;

  if (!actionResult.ledgerVisible) {
    throw new Error(`voucher ${targetVoucherId} ledger entry not visible`);
  }

  result.actions.push(actionResult);
}

async function runUserActionFlow() {
  const seedEmail = `user${Date.now().toString().slice(-5)}@quant.io`;
  const createUserRes = await page.request.post(`${apiBase}/api/users`, {
    headers: apiAuthHeaders,
    data: {
      email: seedEmail,
      name: "E2E User",
      status: "ACTIVE",
      roleCodes: ["VIEWER"]
    }
  });
  if (!createUserRes.ok()) {
    throw new Error(`user seed failed: ${createUserRes.status()}`);
  }
  const createdUser = await createUserRes.json();

  const userFilter = encodeURIComponent(JSON.stringify({ email: seedEmail }));
  await page.goto(
    `${baseUrl}/#/users?filter=${userFilter}&page=1&perPage=25&sort=userId&order=DESC&t=${Date.now()}`,
    { waitUntil: "domcontentloaded" }
  );

  const row = page.locator("tbody tr", {
    has: page.locator("td", { hasText: seedEmail })
  });
  await row.first().waitFor({ timeout: 10000 });

  const lockAt = Date.now();
  await row.first().getByRole("button", { name: "잠금" }).click();
  await assertApiResponse({ since: lockAt, method: "PUT", apiPath: `/api/users/${createdUser.userId}/status` });

  const resetAt = Date.now();
  await row.first().getByRole("button", { name: "비번초기화" }).click();
  await assertApiResponse({ since: resetAt, method: "POST", apiPath: `/api/users/${createdUser.userId}/reset-password` });

  const deleteAt = Date.now();
  await row.first().getByRole("button", { name: "삭제" }).click();
  await assertApiResponse({ since: deleteAt, method: "DELETE", apiPath: `/api/users/${createdUser.userId}` });

  result.actions.push({ key: "user-actions", ok: true, userId: createdUser.userId, email: seedEmail, deleted: true });
}

async function runOrderDeleteFlow() {
  const symbol = `DEL${Date.now().toString().slice(-4)}`;
  const createOrderRes = await page.request.post(`${apiBase}/api/orders`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      symbol,
      side: "BUY",
      orderType: "MARKET",
      timeInForce: "DAY",
      quantity: 1
    }
  });
  if (!createOrderRes.ok()) {
    throw new Error(`order seed for delete failed: ${createOrderRes.status()}`);
  }
  const createdOrder = await createOrderRes.json();

  const orderFilter = encodeURIComponent(JSON.stringify({ portfolioId: 1, symbol }));
  await page.goto(
    `${baseUrl}/#/orders?filter=${orderFilter}&page=1&perPage=25&sort=orderId&order=DESC&t=${Date.now()}`,
    { waitUntil: "domcontentloaded" }
  );

  const row = page.locator("tbody tr", {
    has: page.locator("td", { hasText: symbol })
  });
  await row.first().waitFor({ timeout: 10000 });

  const deleteAt = Date.now();
  await row.first().getByRole("button", { name: "삭제" }).click();
  await assertApiResponse({ since: deleteAt, method: "DELETE", apiPath: `/api/orders/${createdOrder.orderId}` });

  result.actions.push({ key: "order-delete", ok: true, orderId: createdOrder.orderId, symbol });
}

async function runOrderLifecycleFlow() {
  const cancelSymbol = `CAN${Date.now().toString().slice(-4)}`;
  const rejectSymbol = `REJ${Date.now().toString().slice(-4)}`;

  const cancelOrderRes = await page.request.post(`${apiBase}/api/orders`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      symbol: cancelSymbol,
      side: "BUY",
      orderType: "MARKET",
      timeInForce: "DAY",
      quantity: 1
    }
  });
  if (!cancelOrderRes.ok()) {
    throw new Error(`cancel seed order failed: ${cancelOrderRes.status()}`);
  }
  const cancelOrder = await cancelOrderRes.json();

  const rejectOrderRes = await page.request.post(`${apiBase}/api/orders`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      symbol: rejectSymbol,
      side: "BUY",
      orderType: "LIMIT",
      timeInForce: "DAY",
      limitPrice: 120.25,
      quantity: 1
    }
  });
  if (!rejectOrderRes.ok()) {
    throw new Error(`reject seed order failed: ${rejectOrderRes.status()}`);
  }
  const rejectOrder = await rejectOrderRes.json();

  const cancelFilter = encodeURIComponent(JSON.stringify({ portfolioId: 1, symbol: cancelSymbol }));
  await page.goto(
    `${baseUrl}/#/orders?filter=${cancelFilter}&page=1&perPage=25&sort=orderId&order=DESC&t=${Date.now()}`,
    { waitUntil: "domcontentloaded" }
  );

  const cancelRow = page.locator("tbody tr", {
    has: page.locator("td", { hasText: cancelSymbol })
  });
  await cancelRow.first().waitFor({ timeout: 10000 });
  const cancelAt = Date.now();
  await cancelRow.first().getByRole("button", { name: "취소" }).click();
  await assertApiResponse({ since: cancelAt, method: "POST", apiPath: `/api/orders/${cancelOrder.orderId}/cancel` });

  const rejectFilter = encodeURIComponent(JSON.stringify({ portfolioId: 1, symbol: rejectSymbol }));
  await page.goto(
    `${baseUrl}/#/orders?filter=${rejectFilter}&page=1&perPage=25&sort=orderId&order=DESC&t=${Date.now()}`,
    { waitUntil: "domcontentloaded" }
  );
  const rejectRow = page.locator("tbody tr", {
    has: page.locator("td", { hasText: rejectSymbol })
  });
  await rejectRow.first().waitFor({ timeout: 10000 });
  const rejectAt = Date.now();
  await rejectRow.first().getByRole("button", { name: "거부" }).click();
  await assertApiResponse({ since: rejectAt, method: "POST", apiPath: `/api/orders/${rejectOrder.orderId}/reject` });

  result.actions.push({
    key: "order-lifecycle",
    ok: true,
    canceledOrderId: cancelOrder.orderId,
    rejectedOrderId: rejectOrder.orderId
  });
}

async function runOrderAdvancedFilterFlow() {
  const symbol = `FLT${Date.now().toString().slice(-5)}`;
  const createOrderRes = await page.request.post(`${apiBase}/api/orders`, {
    headers: apiAuthHeaders,
    data: {
      portfolioId: 1,
      symbol,
      side: "BUY",
      orderType: "LIMIT",
      timeInForce: "DAY",
      limitPrice: 131.25,
      quantity: 3
    }
  });
  if (!createOrderRes.ok()) {
    throw new Error(`advanced filter seed order failed: ${createOrderRes.status()}`);
  }

  const orderFilter = encodeURIComponent(
    JSON.stringify({
      portfolioId: 1,
      symbol,
      side: "BUY",
      orderType: "LIMIT",
      timeInForce: "DAY",
      minQuantity: 3,
      maxQuantity: 3
    })
  );
  await page.goto(
    `${baseUrl}/#/orders?filter=${orderFilter}&page=1&perPage=25&sort=orderId&order=DESC&t=${Date.now()}`,
    { waitUntil: "domcontentloaded" }
  );

  const row = page.locator("tbody tr", {
    has: page.locator("td", { hasText: symbol })
  });
  await row.first().waitFor({ timeout: 10000 });
  const rowText = await row.first().innerText();
  if (!rowText.includes("BUY") || !rowText.includes("LIMIT")) {
    throw new Error("orders: advanced filter result row values mismatch");
  }

  const rowCount = await page.locator("tbody tr").count();
  if (rowCount < 1) {
    throw new Error("orders: advanced filter returned no rows");
  }

  await page.screenshot({ path: `${outDir}/frontend-orders-advanced-filter.png`, fullPage: true });
  result.actions.push({
    key: "order-advanced-filter",
    ok: true,
    symbol,
    rowCount
  });
}

async function runSessionRevokeFlow() {
  const sessionsRes = await page.request.get(`${apiBase}/api/account/sessions`, { headers: apiAuthHeaders });
  if (!sessionsRes.ok()) {
    throw new Error(`account sessions fetch failed: ${sessionsRes.status()}`);
  }
  const sessionJson = await sessionsRes.json();
  const active = Array.isArray(sessionJson.items)
    ? sessionJson.items.find((x) => x.active === true)
    : null;

  if (!active) {
    throw new Error("no active session to revoke");
  }

  await page.goto(`${baseUrl}/#/accountSessions?t=${Date.now()}`, { waitUntil: "domcontentloaded" });

  const enabledRevokeButton = page.locator("tbody tr button[aria-label='세션해지']:not([disabled])").first();
  await enabledRevokeButton.waitFor({ timeout: 10000 });

  const revokeAt = Date.now();
  await enabledRevokeButton.click();
  await assertApiResponse({ since: revokeAt, method: "POST", apiPath: `/api/account/sessions/${active.sessionId}/revoke` });

  result.actions.push({ key: "session-revoke", ok: true, sessionId: active.sessionId });
}

try {
  await createApiAuthHeaders();
  await loginIfNeeded();
  const seeded = await seedTradeVoucherFlow();
  await runVoucherActionFlow(seeded.voucher.voucherId);
  await seedApprovedVoucherForDashboard();
  await seedDraftVoucherForDashboard();
  await seedEmergencyOpenOrderForDashboard();
  await seedCriticalRiskForDashboard();
  await runDashboardChecks();
  await runListChecks();
  await runUserActionFlow();
  await runOrderDeleteFlow();
  await runOrderLifecycleFlow();
  await runOrderAdvancedFilterFlow();
  await runSessionRevokeFlow();

  result.finishedAt = new Date().toISOString();
  result.ok = true;
  fs.writeFileSync(`${outDir}/frontend-full-e2e-result.json`, JSON.stringify(result, null, 2));
} catch (error) {
  result.finishedAt = new Date().toISOString();
  result.ok = false;
  result.error = error instanceof Error ? error.message : String(error);
  fs.writeFileSync(`${outDir}/frontend-full-e2e-result.json`, JSON.stringify(result, null, 2));
  throw error;
} finally {
  await context.close();
  await browser.close();
}

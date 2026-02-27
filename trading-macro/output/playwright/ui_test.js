const { chromium } = require('playwright');
const FRONTEND = 'http://localhost:5174';
const API_BASE = 'http://localhost:8081/api';
const resources = [
  'strategies','macros','trades','portfolios','risk-policies','performance',
  'performance-summaries','teams','desks','books','menus','menu-permissions'
];

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1400, height: 900 } });
  const results = [];

  const waitForApi = async (resource) => {
    const url = `${API_BASE}/${resource}`;
    const resp = await page.waitForResponse(r => r.url() === url, { timeout: 10000 });
    return resp.status();
  };

  try {
    await page.goto(FRONTEND, { waitUntil: 'networkidle' });
    await page.getByLabel('Username').fill('admin@tm.local');
    await page.getByLabel('Password').fill('admin1234');
    await page.getByRole('button', { name: 'Sign in' }).click();
    await page.waitForURL(url => !url.href.includes('#/login'), { timeout: 10000 });
    const loginUrl = page.url();

    for (const r of resources) {
      await page.goto(`${FRONTEND}/#/${r}`, { waitUntil: 'domcontentloaded' });
      try {
        const status = await waitForApi(r);
        results.push({ resource: r, status });
      } catch (e) {
        results.push({ resource: r, status: 'timeout' });
      }
    }

    await page.screenshot({ path: '/Users/revy/workspace_codex/trading-macro/output/playwright/ui_after_login.png', fullPage: true });
    console.log(JSON.stringify({ loginUrl, results }, null, 2));
  } catch (e) {
    console.error('TEST_FAIL', e.message);
    await page.screenshot({ path: '/Users/revy/workspace_codex/trading-macro/output/playwright/ui_test_fail.png', fullPage: true });
    process.exitCode = 1;
  } finally {
    await browser.close();
  }
})();

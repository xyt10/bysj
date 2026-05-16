import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  const errors = [];
  const consoleLogs = [];
  
  page.on('console', msg => {
    const text = msg.text();
    consoleLogs.push(`[${msg.type()}] ${text}`);
    console.log(`CONSOLE [${msg.type()}]: ${text}`);
  });
  
  page.on('pageerror', err => {
    errors.push(`PAGE ERROR: ${err.message}`);
    console.log(`PAGE ERROR: ${err.message}`);
  });
  
  page.on('requestfailed', req => {
    errors.push(`REQUEST FAILED: ${req.url()}`);
    console.log(`REQUEST FAILED: ${req.url()}`);
  });
  
  console.log('Navigating to http://localhost:13000...');
  await page.goto('http://localhost:13000', { waitUntil: 'networkidle', timeout: 30000 });
  
  await new Promise(r => setTimeout(r, 5000));
  
  const html = await page.content();
  console.log('\n=== PAGE TITLE ===');
  const title = await page.title();
  console.log(title);
  
  const appContent = await page.$eval('#app', el => el.innerHTML).catch(() => 'APP ELEMENT NOT FOUND OR EMPTY');
  console.log('\n=== APP CONTENT ===');
  console.log(appContent.substring ? appContent.substring(0, 2000) : appContent);
  
  console.log('\n=== SUMMARY ===');
  console.log(`Console logs: ${consoleLogs.length}`);
  console.log(`Errors: ${errors.length}`);
  
  await browser.close();
})();

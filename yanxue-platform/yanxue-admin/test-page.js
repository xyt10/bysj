import puppeteer from 'puppeteer';

(async () => {
  const browser = await puppeteer.launch({ headless: 'new', args: ['--no-sandbox'] });
  const page = await browser.newPage();
  
  page.on('console', msg => console.log('CONSOLE:', msg.text()));
  page.on('pageerror', err => console.log('PAGE ERROR:', err.message));
  page.on('requestfailed', req => console.log('REQUEST FAILED:', req.url()));
  
  await page.goto('http://localhost:13000', { waitUntil: 'networkidle2', timeout: 30000 });
  
  await new Promise(r => setTimeout(r, 3000));
  
  const html = await page.content();
  console.log('=== PAGE CONTENT ===');
  console.log(html.substring(0, 3000));
  
  const appContent = await page.$eval('#app', el => el.innerHTML).catch(() => 'APP ELEMENT NOT FOUND OR EMPTY');
  console.log('\n=== APP CONTENT ===');
  console.log(appContent.substring ? appContent.substring(0, 1000) : appContent);
  
  await browser.close();
})();

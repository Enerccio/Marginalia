// Chinese localization: the story editor in Chinese, then back to English.
import {idle, login, main, openTab, sleep, snap} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openTab(page, 'Books');
  await page.mouse.click(146, 774);
  await sleep(500);
  await page.getByRole('option').filter({ hasText: '中文' }).first().click();
  await idle(page, 2500);
  await snap(page, '_zh-workspace');
  // the first row of the table: open the book (pen)
  await page.locator('vaadin-grid vaadin-button:has(vaadin-icon[icon$=":pencil"])').first().click();
  await sleep(2500);
  await page.locator('vaadin-dialog[opened]').first().getByRole('tab').nth(3).click();   // the Story tab (4th)
  await idle(page, 1500);
  await snap(page, 'plugin-chineselocalization');
  // back to English
  await page.getByRole('button').last().click().catch(() => {});
  await idle(page, 800);
  await page.mouse.click(146, 774);
  await sleep(500);
  await page.getByRole('option').filter({ hasText: 'English' }).first().click();
  await idle(page, 2500);
});

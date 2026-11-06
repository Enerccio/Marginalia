// Lorebook VCS: a second revision of the entry of Mizuki (what she knows after the first night).
import {bookTab, login, main, openBook, sleep, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Lorebook');
  // the chevron of the row of Mizuki
  const y = await page.evaluate(() => {
    const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Mizuki Hayashida');
    const r = f.getBoundingClientRect(); return r.y + r.height / 2;
  });
  await page.mouse.click(1725, y);
  await sleep(1200);
  // the grid of the tab is short: bring the opened entry to the top of it
  await page.evaluate(() => { const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Mizuki Hayashida'); f.scrollIntoView({ block: 'start' }); });
  await sleep(600);
  // the + of the revision bar (right end of the bar)
  const plus = await page.evaluate(() => {
    const rev = [...document.querySelectorAll('*')].find(e => e.children.length === 0 && e.textContent.trim() === 'Revision:');
    const ry = rev.getBoundingClientRect().y;
    const btns = [...document.querySelectorAll('vaadin-button')].filter(b => Math.abs(b.getBoundingClientRect().y - ry) < 30 && b.getBoundingClientRect().x > 1500);
    const b = btns.sort((a, c) => c.getBoundingClientRect().x - a.getBoundingClientRect().x)[0];
    const r = b.getBoundingClientRect(); return { x: r.x + r.width / 2, y: r.y + r.height / 2 };
  });
  await page.mouse.click(plus.x, plus.y);
  await sleep(1200);
  // change the content of revision 2
  const area = page.getByLabel('Content', { exact: true }).and(page.locator('textarea')).first();
  const text = await area.inputValue();
  await area.fill(text + '\nAfter the first night: she suspects that Tomáš understood her Japanese from the start and says nothing, because she needs him to translate. Trusts his hands more than his words.');
  await area.dispatchEvent('change');
  await sleep(800);
  await page.evaluate(() => document.activeElement && document.activeElement.blur());
  await snapDialog(page, 'plugin-lorebookvcs', -1);
});

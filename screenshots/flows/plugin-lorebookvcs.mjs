// Lorebook VCS: a second revision of the entry of Draconia (what she is like after the evening in Hogsmeade).
import {bookTab, login, main, openBook, sleep, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Lorebook');
  // the chevron of the row of Draconia
  const y = await page.evaluate(() => {
    const f = [...document.querySelectorAll('vaadin-text-field')].find(x => x.value === 'Draconia Malfoy');
    const r = f.getBoundingClientRect(); return r.y + r.height / 2;
  });
  await page.mouse.click(1725, y);
  await sleep(1200);
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
  await area.fill(text + '\nAfter the evening in Hogsmeade: she knows that Harry has seen her cry and cannot forgive herself for it. Colder towards everybody else, careless with Crabbe and Goyle.');
  await area.dispatchEvent('change');
  await sleep(800);
  await page.evaluate(() => document.activeElement && document.activeElement.blur());
  await snapDialog(page, 'plugin-lorebookvcs', -1);
});

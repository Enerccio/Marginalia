// Character Tracker: the relationships between the characters (added in the dialog of each character).
import {bookTab, idle, login, main, openBook, pick, press, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {relationships} from '../dataset/characters.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Characters');
  for (const [from, to, name, tagline, direction] of relationships) {
    // the pen of the row of `from`
    const y = await page.evaluate(n => {
      const el = [...document.querySelectorAll('*')].find(e => e.children.length === 0 && e.textContent.trim() === n && e.getBoundingClientRect().x < 700);
      const r = el.getBoundingClientRect();
      return r.y + r.height / 2;
    }, from);
    await page.mouse.click(602, y);
    await idle(page, 800);
    const d = page.locator('vaadin-dialog[opened]').last();
    await d.getByRole('button', { name: 'Add relationship' }).scrollIntoViewIfNeeded();
    await pick(d, 'Search character', to);
    if (direction === 'both') {
      await d.locator('vaadin-select').click();
      await sleep(300);
      await page.locator('vaadin-select-item').filter({ hasText: /both|↔/i }).first().click();
    }
    await d.getByLabel('Name', { exact: true }).last().fill(name);
    await d.getByLabel('Name', { exact: true }).last().dispatchEvent('change');
    if (tagline) {
      await d.getByLabel('Tagline', { exact: true }).last().fill(tagline);
      await d.getByLabel('Tagline', { exact: true }).last().dispatchEvent('change');
    }
    await press(d, 'Add relationship', { exact: false });
    await idle(page, 600);
    console.log('  ', from, '->', to, name);
    await press(d, 'Save');
    await idle(page, 700);
  }
  await snap(page, '_rel-done');
});

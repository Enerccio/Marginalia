// Character Tracker: the cast of the book (Characters tab), step by step, with a picture for Harry.
import path from 'node:path';
import {bookTab, fill, idle, login, main, openBook, press, root, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';
import {characters} from '../dataset/characters.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, 'Characters');
  for (const c of characters) {
    await page.getByRole('button', { name: 'Add new character' }).click();
    await idle(page, 700);
    const d = page.locator('vaadin-dialog[opened]').last();
    await fill(d, 'Name', c.name);
    await fill(d, 'Tagline', c.tagline);
    await fill(d, 'Note', c.note);
    await d.locator('input[type=color]').fill(c.color);
    await d.locator('input[type=color]').dispatchEvent('change');
    if (c.icon) {
      await d.locator('vaadin-upload input[type=file]').setInputFiles(path.join(root, 'assets', c.icon));
      await sleep(1500);
    }
    await press(d, 'Save');
    await idle(page, 800);
    console.log('  added', c.name);
  }
  await snap(page, '_characters-list');
});

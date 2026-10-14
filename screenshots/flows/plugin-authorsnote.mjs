// Author's Note: a note that is sent with every generation of the book, and a private one.
import {bookTab, field, fill, login, main, openBook, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 700);
  await sideTab(page, "Author's Note");
  await field(page, 'Insert into prompt').check();
  await sleep(300);
  await fill(page, "Author's Note", 'Keep the fog and the cold in every scene. Draconia is frightened, not cruel: she hides it behind sarcasm. Do not reveal what she is building in the Room of Requirement yet.');
  await fill(page, "Author's Note (private)", 'Plan: next chapter Harry finds the cabinet, Ginny starts to suspect. Ron finds out last. Snape appears at the end.');
  await sleep(800);
  await page.evaluate(() => document.activeElement && document.activeElement.blur());
  await snap(page, 'plugin-authorsnote');
});

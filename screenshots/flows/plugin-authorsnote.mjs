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
  await fill(page, "Author's Note", 'Keep the cold and the wet reeds in every scene. Tomáš hides how well he speaks Japanese, and Mizuki must not find out before the ferry. Do not explain the archway yet.');
  await fill(page, "Author's Note (private)", 'Plan: next chapter the lantern on the river turns out to be a ferry; Saellen speaks first, Steing appears at the end.');
  await sleep(800);
  await page.evaluate(() => document.activeElement && document.activeElement.blur());
  await snap(page, 'plugin-authorsnote');
});

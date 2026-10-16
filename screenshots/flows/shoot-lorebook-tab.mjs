// The read only Lorebook tab of the story editor (nothing is changed). Pictures: story-lorebook-tab.
// State: 13-db-backup (the lorebook of the book has 11 entries).
import {bookTab, idle, login, main, openBook, section, sleep, snap} from '../lib/shot.mjs';
import {widenSidebar} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await section(page, 'lorebook-tab', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    // the second tab of that name: the first is the Lorebook tab of the book window
    await page.getByRole('tab', { name: 'Lorebook', exact: true }).nth(1).click();
    await idle(page, 900);
    // open the details of two entries, the lower one first (the rows below an opened one move down): the arrow at the end of the name
    await page.mouse.click(229, 376);                    // Draconia Malfoy
    await idle(page, 600);
    await page.mouse.click(200, 329);                    // Harry Potter
    await idle(page, 900);
    await sleep(500);
    await snap(page, 'story-lorebook-tab');
  });
});

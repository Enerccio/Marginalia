// Search and replace of the story (menu, window, matches, the warning about summaries) and of a lorebook.
// Pictures: story-settings-menu, search-replace-dialog, search-replace-found, search-replace-warning,
// lorebook-search-replace. State: 13-db-backup; nothing is replaced (the questions are answered with No/Cancel).
import {openStoryMenu} from '../lib/story.mjs';
import {bookTab, closeDialog, closeRestoredBook, dialog, fill, idle, login, main, openBook, openTab, pick, section, sleep, snap, snapDialog, menuItem} from '../lib/shot.mjs';

const openMenu = openStoryMenu;

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');

  await section(page, 'menu', async () => {
    await openMenu(page);
    // the menu opens upwards from the cogs and has grown with every item: the clip is the menu and the bottom bar under it
    const box = await page.evaluate(() => {
      const rects = [...document.querySelectorAll('vaadin-menu-bar-list-box')]
        .map(e => e.getBoundingClientRect()).filter(r => r.width > 50 && r.height > 50);
      const bar = [...document.querySelectorAll('vaadin-horizontal-layout.border-top-subtle')].pop().getBoundingClientRect();
      const left = Math.min(bar.x + 8, ...rects.map(r => r.x)), top = Math.min(...rects.map(r => r.y));
      const right = Math.max(...rects.map(r => r.x + r.width)), bottom = bar.y + bar.height;
      return { x: left - 12, y: top - 12, width: right - left + 24, height: bottom - top + 24 };
    });
    await snap(page, 'story-settings-menu', { clip: box });
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  await section(page, 'story', async () => {
    await openMenu(page);
    await menuItem(page, 'Search and replace').click();
    await idle(page, 800);
    await fill(page, 'Search for', 'Draconia');
    await fill(page, 'Replace with', 'Dracona');
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await sleep(300);
    await snapDialog(page, 'search-replace-dialog', 0);

    await dialog(page).getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.mouse.click(406, 519);                      // open the first row, Story #2: the matches in it
    await idle(page, 900);
    await snapDialog(page, 'search-replace-found', 0);

    // the warning: summaries are not searched and not revalidated, so some of them would become outdated
    await page.getByRole('button', { name: 'Search again', exact: true }).click();
    await idle(page, 600);
    await page.getByLabel('Search in summaries', { exact: true }).uncheck({ force: true });
    await page.getByLabel('Revalidate all summaries', { exact: true }).uncheck({ force: true });
    await dialog(page).getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.getByRole('button', { name: 'Replace', exact: true }).click();
    await idle(page, 1200);
    await snap(page, 'search-replace-warning');
    await page.getByRole('button', { name: 'No', exact: true }).click().catch(() => page.keyboard.press('Escape'));
    await idle(page, 600);
    await closeDialog(page);
  });

  await section(page, 'lorebook', async () => {
    await page.goto('http://localhost:8099/', { waitUntil: 'load' });
    await idle(page, 1000);
    await closeRestoredBook(page);
    await openTab(page, 'Lorebooks');
    await pick(page, 'Lorebook', 'Harry Potter - Half-Blood Prince');
    await sleep(1000);
    await page.getByRole('button', { name: 'Search and replace', exact: true }).click();
    await idle(page, 800);
    await fill(page, 'Search for', 'Draconia');
    await fill(page, 'Replace with', 'Dracona');
    await dialog(page).getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.mouse.click(406, 591);                      // open the row of Draconia Malfoy: the matches in it
    await idle(page, 900);
    await snapDialog(page, 'lorebook-search-replace', 0);
    await closeDialog(page);
  });
});

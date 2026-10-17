// Search and replace of the story (menu, window, matches, the warning about summaries) and of a lorebook.
// Pictures: story-settings-menu, search-replace-dialog, search-replace-found, search-replace-warning,
// lorebook-search-replace. State: 13-db-backup; nothing is replaced (the questions are answered with No/Cancel).
import {bookTab, closeDialog, fill, idle, login, main, openBook, openTab, pick, section, sleep, snap, snapDialog} from '../lib/shot.mjs';

const openMenu = async page => { await page.mouse.click(534, 822); await idle(page, 500); };

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');

  await section(page, 'menu', async () => {
    await openMenu(page);
    await snap(page, 'story-settings-menu', { clip: { x: 410, y: 570, width: 520, height: 330 } });
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  await section(page, 'story', async () => {
    await openMenu(page);
    await page.getByText('Search and replace', { exact: true }).click();
    await idle(page, 800);
    await fill(page, 'Search for', 'Draconia');
    await fill(page, 'Replace with', 'Dracona');
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await sleep(300);
    await snapDialog(page, 'search-replace-dialog', 0);

    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    // open the rows, the lower one first (the rows below an opened one move down)
    await page.mouse.click(406, 627);                      // Story #5
    await idle(page, 600);
    await page.mouse.click(406, 519);                      // Story #2
    await idle(page, 900);
    await snapDialog(page, 'search-replace-found', 0);

    // the warning: summaries are not searched and not revalidated, so some of them would become outdated
    await page.getByRole('button', { name: 'Search again', exact: true }).click();
    await idle(page, 600);
    await page.getByLabel('Search in summaries', { exact: true }).uncheck({ force: true });
    await page.getByLabel('Revalidate all summaries', { exact: true }).uncheck({ force: true });
    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.getByRole('button', { name: 'Replace', exact: true }).click();
    await idle(page, 1200);
    await snapDialog(page, 'search-replace-warning', 0);
    await page.getByRole('button', { name: 'No', exact: true }).click().catch(() => page.keyboard.press('Escape'));
    await idle(page, 600);
    await closeDialog(page);
  });

  await section(page, 'lorebook', async () => {
    await page.goto('http://localhost:8099/', { waitUntil: 'load' });
    await idle(page, 1000);
    await openTab(page, 'Lorebooks');
    await pick(page, 'Lorebook', 'Harry Potter - Half-Blood Prince');
    await sleep(1000);
    await page.getByRole('button', { name: 'Search and replace', exact: true }).click();
    await idle(page, 800);
    await fill(page, 'Search for', 'Draconia');
    await fill(page, 'Replace with', 'Dracona');
    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.getByRole('button', { name: 'Search', exact: false }).first().blur().catch(() => {});
    await snapDialog(page, 'lorebook-search-replace', 0);
    await closeDialog(page);
  });
});

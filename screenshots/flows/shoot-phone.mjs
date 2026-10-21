// Phone screenshots (Chrome with a phone profile: 402x874, touch, mobile user agent). Run on the final state; changes nothing.
import {closeDialog, fill, goto, idle, login, main, section, sleep, snap, snapDialog, users} from '../lib/shot.mjs';

const drawer = async (page, item) => {
  await page.mouse.click(28, 27);                           // the burger of the top bar
  await sleep(600);
  if (item) { await page.getByText(item, { exact: true }).last().click(); await idle(page, 900); }
};

await main(async ({ page }) => {
  await login(page);
  await sleep(800);

  await section(page, 'list', async () => {
    await snap(page, 'phone-book-list');
    await drawer(page);
    await snap(page, 'phone-workspace-drawer');
    await page.mouse.click(390, 400);                        // outside: closes the drawer
    await sleep(500);
  });

  await section(page, 'story', async () => {
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":pencil"])').first().click();   // the pen of the first book (Clone is next to it now)
    await sleep(2500);
    await page.getByRole('tab', { name: 'Story', exact: true }).first().click();
    await idle(page, 1500);
    await page.mouse.click(349, 173);                        // the settings menu of the story: reading style
    await sleep(500);
    await page.getByText('Change Styles For Text', { exact: true }).click();
    await idle(page, 800);
    await page.evaluate(() => { const p = document.querySelector('.story-scroll-panel'); if (p) p.scrollTop = p.scrollHeight; });
    await sleep(500);
    await snap(page, 'phone-book-story');
    // the search and replace window of the story, in the phone layout (nothing is replaced)
    await page.mouse.click(349, 173);
    await sleep(500);
    await page.getByText('Search and replace', { exact: true }).click();
    await idle(page, 800);
    await fill(page, 'Search for', 'Draconia');
    await fill(page, 'Replace with', 'Dracona');
    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'phone-search-replace');
    await closeDialog(page);
    await page.mouse.click(349, 62);                         // the close button of the dialog
    await sleep(1000);
  });

  await section(page, 'lorebook', async () => {
    await drawer(page, 'Lorebooks');
    await sleep(500);
    await snap(page, 'phone-lorebook');
    // the search and replace window of the lorebook (the icon button next to the Enabled check box)
    await page.mouse.click(227, 171);                        // the button next to the Enabled check box
    await idle(page, 800);
    await fill(page, 'Search for', 'Draconia');
    await fill(page, 'Replace with', 'Dracona');
    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'phone-lorebook-search-replace');
    await closeDialog(page);
  });

  await section(page, 'trash', async () => {
    await drawer(page, 'Trash');
    await sleep(500);
    await snap(page, 'phone-trash');
  });

  await section(page, 'viewer', async () => {
    await login(page, users.admin, '/view');                // the viewer has a login of its own
    await sleep(1200);
    await snap(page, 'viewer-list');
    await page.getByText('Harry Potter and Half Blood Prince - FDM', { exact: false }).first().click();
    await idle(page, 2000);
    await snap(page, 'viewer-reading');
  });

  await section(page, 'providers', async () => {
    await goto(page, '/');
    await drawer(page);
    await page.getByRole('button', { name: 'Logout' }).click();
    await idle(page, 1200);
    await login(page, users.reader);
    await drawer(page, 'Inference Providers');
    await sleep(600);
    await snap(page, 'phone-providers');
  });
}, { phone: true });

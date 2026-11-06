// Phone screenshots (Chrome with a phone profile: 402x874, touch, mobile user agent). Run on the final state; changes nothing.
import {closeDialog, fill, goto, idle, login, main, section, sleep, menuItem, snap, snapDialog, users} from '../lib/shot.mjs';

const drawer = async (page, item) => {
  await page.mouse.click(28, 27);                           // the burger of the top bar
  await sleep(600);
  if (item) { await page.getByText(item, { exact: true }).last().click(); await idle(page, 900); }
};

await main(async ({ page }) => {
  await login(page);
  await sleep(800);
  await drawer(page); await page.getByRole('tab', { name: 'Books', exact: true }).click(); await idle(page, 900); await page.mouse.click(390, 400); await sleep(600);   // the workspace remembers the last tab of the flows before: start on the books

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
    await menuItem(page, 'Change Styles For Text').click();      // a sub menu of the styles (Appearance): the Book style is the serif reading look
    await idle(page, 500);
    await page.getByRole('menuitem', { name: 'Book', exact: true }).first().click();
    await idle(page, 800);
    await page.evaluate(() => { const p = document.querySelector('.story-scroll-panel'); if (p) p.scrollTop = p.scrollHeight; });
    await sleep(500);
    await snap(page, 'phone-book-story');
    // the search and replace window of the story, in the phone layout (nothing is replaced)
    await page.mouse.click(349, 173);
    await sleep(500);
    await menuItem(page, 'Search and replace').click();
    await idle(page, 800);
    await fill(page, 'Search for', 'Mizuki');
    await fill(page, 'Replace with', 'Mizuko');
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
    await fill(page, 'Search for', 'Mizuki');
    await fill(page, 'Replace with', 'Mizuko');
    await page.getByRole('button', { name: 'Search', exact: true }).click();
    await page.getByText('Found ', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 1200);
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'phone-lorebook-search-replace');
    await closeDialog(page);
    // the menu of the lorebook, its settings and the form of an entry (nothing is changed)
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":ellipsis-v"])').first().click();
    await sleep(600);
    await snap(page, 'phone-lorebook-menu');
    await page.keyboard.press('Escape');
    await sleep(300);
    await page.getByText('Settings', { exact: true }).last().click();
    await sleep(600);
    await snap(page, 'phone-lorebook-settings');
    await page.getByText('Settings', { exact: true }).last().click();
    await sleep(300);
    await page.locator('vaadin-grid vaadin-button:has(vaadin-icon[icon$=":angle-down"])').first().click();
    await idle(page, 800);
    await snap(page, 'phone-lorebook-entry');
  });

  await section(page, 'books-menu', async () => {
    await drawer(page, 'Books');
    await sleep(500);
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":ellipsis-v"])').first().click();
    await sleep(600);
    await snap(page, 'phone-books-menu');
    await page.keyboard.press('Escape');
    await sleep(300);
  });

  await section(page, 'resources', async () => {
    await drawer(page, 'Resources');
    await sleep(800);
    await snap(page, 'phone-resources');
  });

  await section(page, 'admin', async () => {
    await drawer(page, 'Admin');
    await sleep(800);
    await snap(page, 'phone-admin-users');
    await page.getByRole('tab', { name: 'Database Backups', exact: true }).click();
    await idle(page, 900);
    await snap(page, 'phone-admin-backups');
  });

  await section(page, 'trash', async () => {
    await drawer(page, 'Trash');
    await sleep(500);
    await snap(page, 'phone-trash');
  });

  await section(page, 'viewer', async () => {
    await login(page, users.admin, '/view');                // the viewer has a login of its own
    await sleep(1200);
    await page.getByText('Unregistered', { exact: false }).first().click();
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
    await page.mouse.click(390, 400);                        // the tab can be the one the workspace restored: then the drawer stays open
    await sleep(500);
    await snap(page, 'phone-providers');
  });
}, { phone: true });

// The version history of a part (1.6.0): the part gets versions (an edit by the author, a redraft, a saved version with a name),
// the history window (history-dialog, history-clear), the Include versions check box of the branch view with a part found
// through an old version (history-search), the settings menu with the book wide actions (history-menu) and the phone layout
// (phone-history). Run on a COPY of 13-db-backup: it changes the last part and the Info of nothing else.
import {bookTab, closeDialog, fill, idle, launch, login, main, openBook, press, section, sleep, snap, snapDialog, menuItem} from '../lib/shot.mjs';
import {clickCogs, parts, partAction} from '../lib/story.mjs';

// the open menu: the list box of the last menu that has items
const openMenuRect = page => page.evaluate(() => {
  const r = [...document.querySelectorAll('vaadin-menu-bar-list-box, vaadin-context-menu-list-box')]
    .map(e => e.getBoundingClientRect()).filter(b => b.width > 50 && b.height > 50).pop();
  return { x: r.x, y: r.y, width: r.width, height: r.height };
});

const WORD = 'lantern';        // a word only the first edit has: it is gone from the text afterwards

await main(async ({ page }) => {
  await login(page);
  await openBook(page);

  await section(page, 'versions', async () => {
    await bookTab(page, 'Story');
    await idle(page, 800);

    // 1. the author changes the last part (a version: Author)
    await partAction(page, -1, 'Edit');
    await idle(page, 600);
    let area = parts(page).last().locator('textarea').first();
    let text = await area.inputValue();
    await area.fill(`A ${WORD} swung from the hook by the door, and nobody could say who had lit it.\n\n` + text);
    await area.dispatchEvent('change');
    await partAction(page, -1, 'Save');
    await idle(page, 1000);

    // 2. a second change that removes the sentence again
    await partAction(page, -1, 'Edit');
    await idle(page, 600);
    area = parts(page).last().locator('textarea').first();
    text = await area.inputValue();
    await area.fill('The hook by the door stayed empty.\n\n' + text.replace(/^A lantern swung[^\n]*\n\n/, ''));
    await area.dispatchEvent('change');
    await partAction(page, -1, 'Save');
    await idle(page, 1000);

    // 3. the model redrafts the part (a version: Redrafted)
    await partAction(page, -1, 'Redraft');
    const redraft = page.locator('vaadin-dialog[opened]').last();
    await fill(redraft, 'Instructions for the redraft', 'Make the end quieter; keep the last line.');
    await press(redraft, 'Redraft', { wait: 600 });
    await redraft.getByText('Redrafted text', { exact: false }).first().waitFor({ timeout: 30000 });
    await idle(page, 2500);
    await press(redraft, 'Apply', { wait: 1200 });
    await idle(page, 1200);
  });

  await section(page, 'menu', async () => {
    // the menu of the part with the History item (the count says how many versions there are)
    const part = parts(page).last();
    await part.scrollIntoViewIfNeeded();
    await part.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
    await idle(page, 500);
    const menu = await openMenuRect(page);
    await snap(page, 'history-part-menu', { clip: { x: menu.x - 6, y: menu.y - 6, width: menu.width + 12, height: menu.height + 12 } });
    await page.keyboard.press('Escape');
    await idle(page, 400);
  });

  await section(page, 'dialog', async () => {
    await partAction(page, -1, 'History');
    await page.getByText('Version history of the part', { exact: false }).first().waitFor();
    await idle(page, 900);
    // the window holds the versions of this part only; pick the one of the first edit, with the changes against the previous one
    const rows = page.locator('vaadin-dialog[opened] vaadin-grid').last().locator('vaadin-grid-cell-content');
    await rows.filter({ hasText: "Author" }).nth(1).click();      // the first edit, the one with the lantern (the newest Author is the second edit)
    await idle(page, 1000);
    // a name for that version
    await page.getByRole('button', { name: 'Label…', exact: true }).click();
    await idle(page, 600);
    const ask = page.locator('vaadin-dialog[opened]').last();
    await ask.locator('input, textarea').first().fill('With the lantern');
    await ask.getByRole('button', { name: 'Yes', exact: true }).click();
    await idle(page, 1000);
    await snapDialog(page, 'history-dialog', 0);

    // Clear history: what would go
    await page.getByRole('button', { name: 'Clear history', exact: true }).click();
    await idle(page, 900);
    await snapDialog(page, 'history-clear', 0);
    await closeDialog(page);                                // the question
    await idle(page, 500);
    await closeDialog(page);                                // the history
    await idle(page, 600);
  });

  await section(page, 'search', async () => {
    await bookTab(page, 'Branch view');
    await idle(page, 800);
    await page.mouse.click(250, 160);                       // the search box of the branch view
    await page.keyboard.type(WORD);
    await page.keyboard.press('Enter');
    await sleep(1200);
    await page.getByText('Include versions', { exact: true }).first().click();      // the check box: the search is made again
    await sleep(1500);
    // the found part is further down the tree: scroll it into view
    await page.evaluate(() => document.querySelector('.search-hit')?.scrollIntoView({ block: 'center' }));
    await sleep(600);
    await snap(page, 'history-search');
  });

  await section(page, 'book-menu', async () => {
    await bookTab(page, 'Story');
    await idle(page, 800);
    await clickCogs(page);
    await idle(page, 500);
    const menu = await openMenuRect(page);
    await snap(page, 'history-menu', { clip: { x: menu.x - 12, y: menu.y - 12, width: menu.width + 24, height: menu.height + 24 } });
    await page.keyboard.press('Escape');
  });

  // the phone layout: the same book, the History item of the part menu
  await section(page, 'phone', async () => {
    const phone = await launch({ phone: true });
    try {
      const p = phone.page;
      await login(p);
      await sleep(800);
      await p.locator('vaadin-button:has(vaadin-icon[icon$=":pencil"])').first().click();
      await sleep(2500);
      await p.getByRole('tab', { name: 'Story', exact: true }).first().click();
      await idle(p, 1500);
      await p.evaluate(() => { const s = document.querySelector('.story-scroll-panel'); if (s) s.scrollTop = s.scrollHeight; });
      await sleep(500);
      // the menu of the last part: the hamburger of the line under its text
      await p.locator('vaadin-button:has(vaadin-icon[icon="fas:hamburger"])').last().click();
      await idle(p, 500);
      await menuItem(p, 'History').click();
      await p.getByText('Version history of the part', { exact: false }).first().waitFor();
      await idle(p, 1200);
      // the page of the window scrolls: down to the text of the version
      await p.mouse.move(200, 600);
      await p.mouse.wheel(0, 900);
      await sleep(500);
      await snap(p, 'phone-history');
    } finally {
      await phone.browser.close();
    }
  });
});

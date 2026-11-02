// Export and import of the summaries: the menu of the Summaries window, the file downloaded and edited, the import window.
// Pictures: summaries-import (the window after the edited file was chosen), phone-summaries-import (the same on a phone). State: 13-db-backup; the import is not pressed (a copy
// would be discarded anyway), so nothing changes.
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import {bookTab, dialog, idle, login, main, openBook, press, sleep, snap, snapDialog} from '../lib/shot.mjs';
import {clickSummaries} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await clickSummaries(page);
  await idle(page, 800);

  // the export: a download of the text file
  const [download] = await Promise.all([
    page.waitForEvent('download'),
    dialog(page).getByRole('menuitem', { name: 'Export Summaries' }).click(),
  ]);
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'summaries-'));
  const file = path.join(dir, download.suggestedFilename());
  await download.saveAs(file);
  const text = fs.readFileSync(file, 'utf8');
  console.log('  exported ' + download.suggestedFilename() + ', ' + text.split('\n').length + ' lines');

  // the author edits the first summary in an editor and leaves the others
  const edited = text.replace(/(<!-- summary [^>]*-->\n)/, '$1The two friends meet at the station (edited by hand).\n\n');
  fs.writeFileSync(file, edited);

  await dialog(page).getByRole('menuitem', { name: 'Import Summaries' }).click();
  await idle(page, 600);
  await page.locator('vaadin-dialog[opened] input[type=file]').last().setInputFiles(file);
  await sleep(1500);
  await snapDialog(page, 'summaries-import', 1);

  await press(dialog(page), 'Cancel');
  await idle(page, 400);
});

// the same window on a phone: the Summaries item is in the top bar of the story
await main(async ({ page }) => {
  await login(page);
  await sleep(800);
  await page.locator('vaadin-button:has(vaadin-icon[icon$=":pencil"])').first().click();
  await sleep(2500);
  await page.getByRole('tab', { name: 'Story', exact: true }).first().click();
  await idle(page, 1500);
  await page.locator('vaadin-menu-bar-button:has(vaadin-icon[icon="fas:list-ul"])').first().click();
  await idle(page, 900);
  await page.locator('vaadin-dialog[opened] vaadin-menu-bar-button[slot=overflow]:not([hidden])').click();   // the menu bar is narrow: the items are behind the dots
  await idle(page, 500);
  await page.getByRole('menuitem', { name: 'Import Summaries' }).click();
  await idle(page, 600);
  const file = fs.readdirSync(os.tmpdir()).filter(n => n.startsWith('summaries-')).map(n => path.join(os.tmpdir(), n)).pop();
  const exported = path.join(file, fs.readdirSync(file)[0]);
  await page.locator('vaadin-dialog[opened] input[type=file]').last().setInputFiles(exported);
  await sleep(1500);
  await snap(page, 'phone-summaries-import');
}, { phone: true });

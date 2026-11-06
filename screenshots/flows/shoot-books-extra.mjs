// What the books and the lorebooks got in 1.4.0: tagging many at once, a lorebook with subbooks (Count with subbooks, Mass
// edit), importing a zip of .md files. Run on a COPY of 13-db-backup, it is discarded: a lorebook with two subbooks is imported
// (dataset/lorebook.mjs, `companion`) and a book is imported from the archive.
// Pictures: multi-tag-books, lorebook-sub-lorebooks, lorebook-mass-edit, multi-tag-entries, archive-upload, archive-entry-names,
// archive-progress, archive-lorebook-upload.
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {bookListMenu, closeDialog, dialog, idle, login, main, openTab, outDir, pick, press, root, section, sleep, snap, snapDialog} from '../lib/shot.mjs';

execFileSync('node', [path.join(root, 'dataset/lorebook.mjs')], { stdio: 'inherit' });

// the archive: the files are original text, zipped here (the name of the zip is the name of the new book)
const archiveDir = path.join(outDir, '_archive');
const zipOf = (zipName, files) => {
  const dir = path.join(archiveDir, zipName.replace(/\.zip$/, ''));
  fs.rmSync(dir, { recursive: true, force: true });
  fs.mkdirSync(dir, { recursive: true });
  for (const [name, text] of Object.entries(files)) fs.writeFileSync(path.join(dir, name), text);
  const zip = path.join(archiveDir, zipName);
  fs.rmSync(zip, { force: true });
  execFileSync('zip', ['-j', '-q', zip, ...Object.keys(files).map(n => path.join(dir, n))]);
  return zip;
};
fs.mkdirSync(archiveDir, { recursive: true });
const bookZip = zipOf('Notes from the Cellar.zip', {
  '01 The Stairs.md': 'The stone stairs were wet when the last group came up from the cellar. Nobody spoke; breath hung in the air in small clouds.\n',
  '02 The Iron Ring.md': 'The iron ring in the wall smelled of rust and old rope. Everybody reached for it, and nobody asked why it was there.\n',
  '03 The Bolt.md': 'The bolt slid home at nine. By then the tour had been counted twice and lost once.\n',
});
const loreZip = zipOf('Valley Places.zip', {
  'The Ferry Landing.md': 'A wooden jetty on the Lyrasil. A lantern on a pole, a broad woman who shouts, and the smell of wet rope.\n',
  'The Weir.md': 'A low dam above the village. The river sounds like applause there.\n',
  'Ledger House.md': 'A square stone building by the bridge. Nobody goes there for pleasure, and everybody has a line in it.\n',
});

await main(async ({ page }) => {
  await login(page);

  // a book from a zip of .md files
  await section(page, 'archive-book', async () => {
    await openTab(page, 'Books');
    await bookListMenu(page, 'Import', 'Import from archive');
    await idle(page, 900);
    await snapDialog(page, 'archive-upload', -1);
    await page.locator('vaadin-upload input[type=file]').last().setInputFiles(bookZip);
    await idle(page, 2500);
    // the book is made and opened
    await page.getByRole('button', { name: 'Exit', exact: true }).first().click();
    await idle(page, 1000);
  });

  await section(page, 'tag-books', async () => {
    await openTab(page, 'Books');
    await bookListMenu(page, 'Manage', 'Tag books');
    await idle(page, 1200);
    await pick(dialog(page), 'Tag', 'portal fantasy');
    await sleep(800);
    await snapDialog(page, 'multi-tag-books', -1);
    await closeDialog(page);
  });

  // a lorebook with two subbooks, imported through the lorebook import
  await section(page, 'subbooks', async () => {
    await openTab(page, 'Lorebooks');
    await sleep(800);
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":file-import"])').first().click();
    await idle(page, 500);
    await page.getByText('Import Marginalia Lorebook', { exact: true }).click();
    await idle(page, 800);
    await page.locator('vaadin-upload input[type=file]').last().setInputFiles(path.join(root, 'dataset/build/companion.json'));
    await idle(page, 3000);
    await snap(page, '_companion-imported');
    // the lorebook it made is the open one
    await page.getByText('Count with subbooks', { exact: true }).click();
    await idle(page, 900);
    await snap(page, 'lorebook-sub-lorebooks');

    await page.getByRole('button', { name: 'Mass edit', exact: true }).click();
    await idle(page, 1800);
    await snapDialog(page, 'lorebook-mass-edit', -1);
    await closeDialog(page);

    await page.locator('vaadin-button:has(vaadin-icon[icon$=":tags"])').nth(1).click();
    await idle(page, 1500);
    await pick(dialog(page), 'Tag', 'places');
    await sleep(800);
    await snapDialog(page, 'multi-tag-entries', -1);
    await closeDialog(page);
  });


  // entries from a zip of .md files: the menu of the import button, the window, the names of the entries
  await section(page, 'archive-lorebook', async () => {
    await openTab(page, 'Lorebooks');
    await sleep(800);
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":file-import"])').first().click();
    await idle(page, 600);
    console.log('MENU', await page.getByRole('menuitem').allInnerTexts());
    await snap(page, '_import-menu');
    await page.getByRole('menuitem', { name: /archive/i }).first().click();
    await idle(page, 900);
    await snapDialog(page, 'archive-lorebook-upload', -1);
    await page.locator('vaadin-upload input[type=file]').last().setInputFiles(loreZip);
    await idle(page, 2500);
    await snapDialog(page, 'archive-entry-names', -1);
    console.log(await dialog(page).innerText());
  });
});

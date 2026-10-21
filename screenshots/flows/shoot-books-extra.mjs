// What the books and the lorebooks got in 1.4.0: tagging many at once, a lorebook with subbooks (Count with subbooks, Mass
// edit), importing a zip of .md files. Run on a COPY of 13-db-backup, it is discarded: a lorebook with two subbooks is imported
// (dataset/lorebook.mjs, `companion`) and a book is imported from the archive.
// Pictures: multi-tag-books, lorebook-sub-lorebooks, lorebook-mass-edit, multi-tag-entries, archive-upload, archive-entry-names,
// archive-progress, archive-lorebook-upload.
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {closeDialog, dialog, idle, login, main, openTab, outDir, pick, press, root, section, sleep, snap, snapDialog} from '../lib/shot.mjs';

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
const bookZip = zipOf('Notes from the Quidditch Pitch.zip', {
  '01 The Frost.md': 'The grass was white with frost when the team came down from the castle. Nobody spoke; breath hung in the air in small clouds.\n',
  '02 The Broom Shed.md': 'The broom shed smelled of polish and old wood. Ginny took the best broom without asking, and nobody asked her to give it back.\n',
  '03 The First Bell.md': 'The first bell rang over the lake. By then the Snitch had been caught twice and lost three times.\n',
});
const loreZip = zipOf('Hogsmeade Places.zip', {
  'The Three Broomsticks.md': 'The inn on the main street. Warm, loud, smells of butterbeer; Madam Rosmerta keeps it.\n',
  'Honeydukes.md': 'The sweet shop. The cellar has a trapdoor that leads under the castle.\n',
  'The Shrieking Shack.md': 'A boarded up house on the hill above the village. Nobody goes there, and everybody says it is haunted.\n',
});

await main(async ({ page }) => {
  await login(page);

  // a book from a zip of .md files
  await section(page, 'archive-book', async () => {
    await openTab(page, 'Books');
    await page.locator('vaadin-button:has(vaadin-icon[icon$=":file-import"])').first().click();
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
    await page.getByRole('button', { name: 'Tag books', exact: true }).click();
    await idle(page, 1200);
    await pick(dialog(page), 'Tag', 'genderbend');
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

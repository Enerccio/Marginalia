// What the books and the lorebooks got in 1.4.0: tagging many at once, a lorebook with subbooks (Count with subbooks, Mass
// edit), importing a zip of .md files. Run on a COPY of 13-db-backup, it is discarded: a lorebook with two subbooks is imported
// (dataset/lorebook.mjs, `companion`) and a book is imported from the archive.
// Pictures: multi-tag-books, lorebook-sub-lorebooks, lorebook-mass-edit, multi-tag-entries, archive-upload, archive-entry-names,
// archive-progress, archive-lorebook-upload, and (1.8.0) import-source (the import window with the Source selector open), document-import (a Word
// document uploaded, with its options), book-import-into (the same window opened from the settings menu of a book, the place picked).
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {openStoryMenu} from '../lib/story.mjs';
import {
  bookListMenu,
  bookTab,
  closeDialog,
  DESKTOP,
  dialog,
  field,
  idle,
  login,
  main,
  menuItem,
  openBook,
  openImport,
  openTab,
  outDir,
  pick,
  root,
  section,
  sleep,
  snap,
  snapDialog
} from '../lib/shot.mjs';

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

// a .docx made by hand (a zip of XML): the styles give "Title" and "heading 1" their built in names, the way Word does
const xml = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
function manuscriptDocx() {
  const parts = JSON.parse(fs.readFileSync(path.join(root, 'dataset/unregistered-parts.json'), 'utf8')).map(p => p.reply);
  const chapters = [['The Cellar Tour', [0, 1]], ['The Bricked Archway', [2, 3]], ['What the River Keeps', [4, 5]]];
  const paragraph = (text, style) => `<w:p>${style ? `<w:pPr><w:pStyle w:val="${style}"/></w:pPr>` : ''}<w:r><w:t xml:space="preserve">${xml(text)}</w:t></w:r></w:p>`;
  let body = paragraph('Unregistered', 'Title');
  for (const [title, indexes] of chapters) {
    body += paragraph(title, 'Heading1');
    for (const i of indexes) for (const text of parts[i].split(/\n\s*\n/)) if (text.trim()) body += paragraph(text.trim().replace(/\s*\n\s*/g, ' '));
  }
  const dir = path.join(archiveDir, '_docx');
  fs.rmSync(dir, { recursive: true, force: true });
  fs.mkdirSync(path.join(dir, '_rels'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'word/_rels'), { recursive: true });
  const ns = 'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"';
  fs.writeFileSync(path.join(dir, '[Content_Types].xml'), '<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
    + '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>'
    + '<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
    + '<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/></Types>');
  fs.writeFileSync(path.join(dir, '_rels/.rels'), '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    + '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>');
  fs.writeFileSync(path.join(dir, 'word/_rels/document.xml.rels'), '<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    + '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>');
  fs.writeFileSync(path.join(dir, 'word/styles.xml'), `<?xml version="1.0" encoding="UTF-8"?><w:styles ${ns}>`
    + '<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/></w:style>'
    + '<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/></w:style></w:styles>');
  fs.writeFileSync(path.join(dir, 'word/document.xml'), `<?xml version="1.0" encoding="UTF-8"?><w:document ${ns}><w:body>${body}</w:body></w:document>`);
  const docx = path.join(archiveDir, 'Unregistered draft.docx');
  fs.rmSync(docx, { force: true });
  execFileSync('zip', ['-q', '-r', docx, '[Content_Types].xml', '_rels', 'word'], { cwd: dir });
  return docx;
}
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
    const d = await openImport(page);
    await idle(page, 400);
    // the selector with its sources
    await field(d, 'Source').click();
    await idle(page, 500);
    await snap(page, 'import-source');
    await page.keyboard.press('Escape');
    await idle(page, 400);
    await page.locator('vaadin-upload input[type=file]').last().setInputFiles(bookZip);
    await idle(page, 2500);
    await snapDialog(page, 'archive-upload', -1);
    await dialog(page).getByRole('button', { name: 'Import', exact: true }).click();
    await idle(page, 3000);
    // the book is made and opened
    await page.getByRole('button', { name: 'Exit', exact: true }).first().click();
    await idle(page, 1000);
  });

  // a Word manuscript: a title, three chapters with Heading 1 (the text of the story of the dataset), cut into parts of about 500 words
  await section(page, 'document-import', async () => {
    await openTab(page, 'Books');
    const d = await openImport(page, 'Import from a document');
    await page.locator('vaadin-upload input[type=file]').last().setInputFiles(manuscriptDocx());
    await idle(page, 2500);
    const words = d.locator('vaadin-integer-field').first();
    await words.locator('input').fill('500');
    await words.locator('input').dispatchEvent('change');
    await idle(page, 800);
    // a taller window, so the dialog shows down to its last option
    await page.setViewportSize({ width: DESKTOP.width, height: 1150 });
    await idle(page, 600);
    await snapDialog(page, 'document-import', -1);
    await page.setViewportSize(DESKTOP);
    await idle(page, 400);
    await dialog(page).getByRole('button', { name: 'Import', exact: true }).click();
    await idle(page, 3500);
    await snap(page, '_document-imported');
    await page.getByRole('button', { name: 'Exit', exact: true }).first().click();
    await idle(page, 1000);
  });

  // the import window from the settings menu of a book: the parts go after the second part of the story, what followed them follows them
  await section(page, 'book-import-into', async () => {
    await openBook(page, 'Notes from the Cellar');
    await bookTab(page, 'Story');
    await openStoryMenu(page);
    await page.getByRole('menuitem', { name: /^Import & export/ }).first().click();
    await idle(page, 400);
    await menuItem(page, 'Import').click();
    await idle(page, 900);
    const d = dialog(page);
    await pick(d, 'Source', 'Import from a document');
    await idle(page, 500);
    await page.locator('vaadin-upload input[type=file]').last().setInputFiles(manuscriptDocx());
    await idle(page, 2500);
    const words = d.locator('vaadin-integer-field').first();
    await words.locator('input').fill('500');
    await words.locator('input').dispatchEvent('change');
    await idle(page, 600);
    await d.locator('vaadin-radio-button').filter({ hasText: 'After a part' }).click();
    await idle(page, 500);
    await field(d, 'Part').click();
    await idle(page, 500);
    await page.getByRole('option').nth(1).click();
    await idle(page, 600);
    await page.setViewportSize({ width: DESKTOP.width, height: 1250 });
    await idle(page, 600);
    await snapDialog(page, 'book-import-into', 0);
    await page.setViewportSize(DESKTOP);
    await idle(page, 400);
    await dialog(page).getByRole('button', { name: 'Import', exact: true }).click();
    await idle(page, 3500);
    // the story shows the new parts between the second and the third
    await snap(page, '_book-imported');
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

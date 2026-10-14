// More book screenshots: the reasoning of a part (needs the branch of the reasoning model), the backup dialogs, the export
// and the import of a backup as a new book (the lorebook decision). Run on step 13-db-backup or later.
import fs from 'node:fs';
import path from 'node:path';
import {
    bookTab,
    closeDialog,
    dialog,
    dialogRects,
    fill,
    idle,
    login,
    main,
    openBook,
    openTab,
    outDir,
    pickSelect,
    press,
    root,
    section,
    sleep,
    snap,
    snapDialog
} from '../lib/shot.mjs';
import {parts, readingStyle} from '../lib/story.mjs';

const tree = async page => { await page.mouse.move(1000, 500); await page.mouse.wheel(0, 700); await sleep(600); };

await main(async ({ page }) => {
  await login(page);
  await openBook(page);

  await section(page, 'reasoning', async () => {
    // switch to the branch of the reasoning model (the leaf on the right), show the reasoning, switch back
    await bookTab(page, 'Branch view');
    await tree(page);
    await page.mouse.click(1030, 764);
    await idle(page, 1500);
    await readingStyle(page, false);
    const last = parts(page).last();
    await last.getByText('View Reasoning', { exact: true }).click();         // expand the reasoning
    await sleep(500);
    await page.evaluate(() => {
      const p = document.querySelector('.story-scroll-panel');
      const el = [...document.querySelectorAll('.chat-message')].pop();
      p.scrollTop = el.offsetTop - 8;
    });
    await sleep(500);
    const r = await last.boundingBox();
    await snap(page, 'reasoning-view', { clip: { x: r.x, y: Math.max(130, r.y), width: r.width, height: Math.min(603, r.height, 920 - Math.max(130, r.y)) } });
    await bookTab(page, 'Branch view');
    await tree(page);
    await page.mouse.click(750, 764);
    await idle(page, 1500);
  });

  await section(page, 'backup-dialogs', async () => {
    await bookTab(page, 'Backups');
    // export of a backup that uses pictures asks whether to pack them (the newest backup has one)
    await page.getByRole('button', { name: 'Export', exact: true }).first().click();
    await idle(page, 700);
    await snap(page, 'backup-export-images');
    await closeDialog(page);
    // clone: the name first, then with or without pictures
    await page.getByRole('button', { name: 'Clone Backup', exact: true }).first().click();
    await idle(page, 700);
    await dialog(page).locator('input').first().fill('Clone for the manual');
    await press(dialog(page), 'Yes').catch(() => press(dialog(page), 'OK'));
    await idle(page, 900);
    await snapDialog(page, 'backup-clone-images');
    await closeDialog(page);
  });

  await section(page, 'export', async () => {
    await bookTab(page, 'Story');
    await page.mouse.click(534, 822);
    await idle(page, 400);
    await page.getByText('Export story', { exact: true }).click();
    await idle(page, 800);
    await snapDialog(page, 'export-dialog');
    // a PDF: the dialog that offers the finished file
    await pickSelect(dialog(page), 'PDF (.pdf)');
    await fill(dialog(page), 'File name', 'Harry Potter and Half Blood Prince - FDM');
    await press(dialog(page), 'Export', { exact: false });
    await page.getByText('The export is ready', { exact: false }).waitFor({ timeout: 30000 });
    await sleep(500);
    await snapDialog(page, 'export-download');
    const [download] = await Promise.all([
      page.waitForEvent('download'),
      dialog(page).getByRole('button', { name: /^Download/ }).click(),
    ]);
    await download.saveAs(path.join(outDir, '_export.pdf'));
    await closeDialog(page);
  });

  await section(page, 'lorebook-import', async () => {
    // the newest backup as a file, with another identity for the lorebook: Marginalia asks whether to link it
    const dir = path.join(root, '.data/.marginalia/data/admin/backups/manuscripts');
    const folder = fs.readdirSync(dir).find(f => fs.readdirSync(path.join(dir, f)).some(x => x.endsWith('.json')));
    const files = fs.readdirSync(path.join(dir, folder)).filter(f => f.endsWith('.json')).sort();
    const backup = JSON.parse(fs.readFileSync(path.join(dir, folder, files[files.length - 1]), 'utf8'));
    const inner = backup.backup ?? backup;
    const oldUuid = inner.lorebook.uuid;
    const text = JSON.stringify(backup).split(oldUuid).join('99999999-9999-4999-8999-999999999999');
    const file = path.join(outDir, '_backup-for-import.json');
    fs.writeFileSync(file, text);
    await press(page, 'Exit');
    await idle(page, 800);
    await openTab(page, 'Settings');
    await press(page, 'Import Backup as New Book', { exact: false });
    await idle(page, 700);
    await fill(dialog(page), 'New Book Name', 'Imported copy');
    await dialog(page).locator('vaadin-upload input[type=file]').setInputFiles(file);
    await sleep(2500);
    const rects = await dialogRects(page);
    console.log('  dialogs:', rects.length);
    const top = rects[0], below = rects[1];
    const u = below ? { x: Math.min(top.x, below.x), y: Math.min(top.y, below.y), width: Math.max(top.x + top.width, below.x + below.width) - Math.min(top.x, below.x), height: Math.max(top.y + top.height, below.y + below.height) - Math.min(top.y, below.y) } : top;
    await snap(page, 'lorebook-import', { clip: u });
    await closeDialog(page);
    await closeDialog(page);
  });
});

// export-result.png: the title page and the table of contents of an exported PDF (needs chapters: run after step 21-chaptermarker).
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {openStoryMenu} from '../lib/story.mjs';
import {bookTab, closeDialog, dialog, idle, login, main, openBook, outDir, pickSelect, press} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await openStoryMenu(page);
  await page.getByText('Export story', { exact: true }).click();
  await idle(page, 800);
  await pickSelect(dialog(page), 'PDF (.pdf)');
  await press(dialog(page), 'Export', { exact: false });
  await page.getByText('The export is ready', { exact: false }).waitFor({ timeout: 30000 });
  const [download] = await Promise.all([
    page.waitForEvent('download'),
    dialog(page).getByRole('button', { name: /^Download/ }).click(),
  ]);
  const pdf = path.join(outDir, '_export-chapters.pdf');
  await download.saveAs(pdf);
  await closeDialog(page);
  // title page and table of contents -> one picture
  for (const n of [1, 2]) execFileSync('pdftoppm', ['-png', '-r', '90', '-f', String(n), '-l', String(n), '-singlefile', pdf, path.join(outDir, `_export-page${n}`)]);
  const page1 = path.join(outDir, '_export-page1.png'), page2 = path.join(outDir, '_export-page2.png');
  execFileSync('magick', ['(', page1, '-bordercolor', '#1c2a3d', '-border', '8', ')', '(', page2, '-bordercolor', '#1c2a3d', '-border', '8', ')', '+append', path.join(outDir, 'export-result.png')]);
  console.log('  saved export-result.png');
});

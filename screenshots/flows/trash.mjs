// Puts things into the trash: a lorebook with entries (Bestiary), a scratch book, a story part of a branch.
import path from 'node:path';
import {dialog, idle, login, main, openTab, press, root, snap} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);

  // the Bestiary lorebook: import, then delete it
  await openTab(page, 'Lorebooks');
  await page.mouse.click(1167, 71);          // the import menu button (next to Delete Lorebook)
  await idle(page, 500);
  await page.getByText('Import Marginalia Lorebook', { exact: true }).click();
  await idle(page, 800);
  await page.locator('vaadin-upload input[type=file]').last().setInputFiles(path.join(root, 'dataset/build/bestiary.json'));
  await idle(page, 2500);
  await press(page, 'Delete Lorebook', { exact: false });
  await idle(page, 600);
  await snap(page, '_delete-lorebook-confirm');
  await press(dialog(page), 'Yes');
  await idle(page, 1000);

  // a scratch book, deleted with the red trash button of its row
  await openTab(page, 'Books');
  await press(page, 'Add Book');
  await dialog(page).locator('input').first().fill('Scratch book');
  await press(dialog(page), 'Yes');
  await page.getByText('Branch view', { exact: true }).first().waitFor();
  await idle(page, 800);
  await press(page, 'Exit');
  await idle(page, 800);
  const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: 'Scratch book' }).first();
  const box = await cell.boundingBox();
  await page.mouse.click(1858, box.y + box.height / 2);
  await idle(page, 600);
  await snap(page, '_delete-book-confirm');
  await press(dialog(page), 'Yes');
  await idle(page, 1000);
  await snap(page, '_books-after-delete');
});

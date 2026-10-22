// Step 04-protocols -> 05-lorebook: imports the lorebook of the demo book (dataset/lorebook.mjs).
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {idle, login, main, openTab, root, snap} from '../lib/shot.mjs';

execFileSync('node', [path.join(root, 'dataset/lorebook.mjs')], { stdio: 'inherit' });

await main(async ({ page }) => {
  await login(page);
  await openTab(page, 'Lorebooks');
  await page.mouse.click(1167, 71);          // the import menu button (next to Delete Lorebook)
  await idle(page, 500);
  await page.getByText('Import Marginalia Lorebook', { exact: true }).click();
  await idle(page, 800);
  await snap(page, '_lore-import-dialog');
  await page.locator('vaadin-upload input[type=file]').last().setInputFiles(path.join(root, 'dataset/build/lorebook.json'));
  await idle(page, 2500);
  await snap(page, '_lore-imported');
});

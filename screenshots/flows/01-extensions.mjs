// Step 01-admin -> 02-extensions: installs every built plugin through Admin -> Extensions.
import fs from 'node:fs';
import path from 'node:path';
import {idle, launch, login, repo, snap} from '../lib/shot.mjs';

const plugins = ['authorsnote', 'chaptermarker', 'character-tracker', 'chineselocalization', 'gemma4toolsfix',
  'instructionexporter', 'lore-generator', 'lorebookvcs', 'name-generator', 'name-provider', 'reviewer', 'sidequery'];

const { browser, page } = await launch();
await login(page);
await page.getByRole('button', { name: 'Admin' }).click();
await idle(page, 600);
await page.getByText('Extensions', { exact: true }).first().click();
await idle(page, 600);

for (const name of plugins) {
  const jar = path.join(repo, 'marginalia/plugins', name, 'target', `${name}-1.3.0.jar`);
  if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
  const rows = await page.locator('vaadin-grid-cell-content', { hasText: name }).count();
  if (rows) { console.log('  already installed', name); continue; }
  await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
  await idle(page, 3000);
  // a confirmation may follow
  const dialog = page.locator('vaadin-confirm-dialog-overlay, vaadin-dialog-overlay');
  if (await dialog.count()) { console.log('  dialog after', name, ':', (await dialog.first().innerText()).slice(0, 200).replace(/\n/g, ' | ')); }
  await page.getByRole('button', { name: 'Refresh' }).click();
  await idle(page, 1500);
  console.log('  installed', name);
}
await snap(page, '_extensions-installed');
await browser.close();

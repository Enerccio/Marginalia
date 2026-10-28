// Anthropic Claude: installs the plugin (a 36 MB JAR), adds a provider of the type Extension and shows its settings and the list.
// Pictures: plugin-anthropic (Per Type Settings: the provider and its settings), plugin-anthropic-list.
// State: a COPY of 13-db-backup (the plugin and the provider are not part of the other pictures, so the data is discarded).
// Nothing is sent to Anthropic: the key is a placeholder and Test Connection is not pressed.
import fs from 'node:fs';
import path from 'node:path';
import {CONTENT, dialog, fill, idle, login, main, openTab, pick, press, repo, sleep, snap, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);

  // install through Admin -> Extensions
  await page.getByRole('button', { name: 'Admin' }).click();
  await idle(page, 600);
  await page.getByText('Extensions', { exact: true }).first().click();
  await idle(page, 800);
  const jar = path.join(repo, 'marginalia/plugins/anthropic/target/anthropic-1.6.0.jar');
  if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
  await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
  await idle(page, 10000);
  await page.getByRole('button', { name: 'Refresh' }).click();
  await idle(page, 1500);
  console.log('  installed anthropic');

  await openTab(page, 'Inference Providers');
  await press(page, 'Add Inference Provider');
  const d = dialog(page);
  await fill(d, 'Name', 'Claude');
  await pick(d, 'Type', 'Extension');
  await idle(page, 600);
  await fill(d, 'Max Context Size', '200000');
  await fill(d, 'Max Response Tokens', '8192');
  await sleep(300);

  // the provider list is on Per Type Settings; with one provider only it is chosen already
  await d.getByText('Per Type Settings', { exact: true }).click();
  await idle(page, 800);
  await fill(d, 'Api Key', 'demo-key');
  await pick(d, 'Model', 'claude-sonnet-5-5');             // Refresh would ask Anthropic; a name is accepted as typed
  await sleep(400);
  await snapDialog(page, 'plugin-anthropic');
  await press(d, 'OK');
  await idle(page, 1200);
  await snap(page, 'plugin-anthropic-list', { clip: CONTENT });
});

// Google Gemini: installs the plugin (a 14 MB JAR), adds a provider of the type Extension and shows its dialog and the list.
// Pictures: plugin-googlegenai-general (type Extension, reasoning), plugin-googlegenai (Per Type Settings: the provider and its settings), plugin-googlegenai-list.
// State: a COPY of 13-db-backup (the plugin and the provider are not part of the other pictures, so the data is discarded).
// Nothing is sent to Google: the key is a placeholder and Test Connection is not pressed.
import fs from 'node:fs';
import path from 'node:path';
import {CONTENT, dialog, field, fill, idle, login, main, openTab, pick, press, repo, sleep, snap, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);

  // install through Admin -> Extensions
  await page.getByRole('button', { name: 'Admin' }).click();
  await idle(page, 600);
  await page.getByText('Extensions', { exact: true }).first().click();
  await idle(page, 800);
  const jar = path.join(repo, 'marginalia/plugins/google-genai/target/google-genai-1.5.0.jar');
  if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
  await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
  await idle(page, 8000);
  await page.getByRole('button', { name: 'Refresh' }).click();
  await idle(page, 1500);
  console.log('  installed google-genai');

  await openTab(page, 'Inference Providers');
  await press(page, 'Add Inference Provider');
  const d = dialog(page);
  await fill(d, 'Name', 'Google Gemini');
  await pick(d, 'Type', 'Extension');
  await idle(page, 600);
  await fill(d, 'Max Context Size', '1048576');
  await fill(d, 'Max Response Tokens', '8192');
  await field(d, 'Enable Reasoning').check();
  await idle(page, 300);
  await pick(d, 'Reasoning Effort', 'Medium');
  // filling the lower fields scrolled the tab: back to the top
  await field(d, 'Max Context Size').evaluate(el => {
    for (let p = el; p; p = p.parentElement || (p.getRootNode() && p.getRootNode().host)) if (p.scrollTop > 0) p.scrollTop = 0;
  });
  await sleep(300);
  await snapDialog(page, 'plugin-googlegenai-general');

  // the Provider list is on Per Type Settings; with one provider only it is chosen already
  await d.getByText('Per Type Settings', { exact: true }).click();
  await idle(page, 800);
  await fill(d, 'Api Key', 'demo-key');
  await pick(d, 'Model', 'gemini-2.5-flash');             // Refresh would ask Google; a name is accepted as typed
  await sleep(400);
  await snapDialog(page, 'plugin-googlegenai');
  await press(d, 'OK');
  await idle(page, 1200);
  await snap(page, 'plugin-googlegenai-list', { clip: CONTENT });
});

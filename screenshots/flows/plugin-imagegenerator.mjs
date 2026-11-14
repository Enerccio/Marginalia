// Image Generator: installs the plugin, adds a backend of the type "OpenAI images API" (the fake server answers /v1/images/generations with one
// picture), then makes an image of the last part: the prompt written by the model, the picture, and the part with the picture.
// Pictures: plugin-imagegenerator-settings (the panel with the backend), plugin-imagegenerator-prompt (the window, the prompt written),
// plugin-imagegenerator (the window with the picture and Reroll / Discard / Accept), plugin-imagegenerator-menu (the item of the menu of a part).
// State: a COPY of 13-db-backup (the plugin, the backend and the picture are not part of the other pictures, so the data is discarded).
import fs from 'node:fs';
import path from 'node:path';
import {
  bookTab, CONTENT, dialog, field, fill, idle, llmUrl, login, main, menuItem, openBook, openTab, pick, press, repo, section, sleep, snap, snapDialog
} from '../lib/shot.mjs';
import {parts} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);

  await section(page, 'install', async () => {
    await page.getByRole('button', { name: 'Admin' }).click();
    await idle(page, 600);
    await page.getByText('Extensions', { exact: true }).first().click();
    await idle(page, 800);
    const jar = path.join(repo, 'marginalia/plugins/image-generator/target/image-generator-1.8.0.jar');
    if (!fs.existsSync(jar)) throw new Error('not built: ' + jar);
    await page.locator('vaadin-upload input[type=file]').setInputFiles(jar);
    await idle(page, 5000);
    await page.getByRole('button', { name: 'Refresh' }).click();
    await idle(page, 1500);
  });

  await section(page, 'settings', async () => {
    await openTab(page, 'Settings');
    await page.getByRole('tab', { name: 'Extension Settings', exact: true }).click();
    await idle(page, 900);
    await page.getByText('Image Generator Settings', { exact: true }).click();
    await idle(page, 900);
    await page.locator('vaadin-button[aria-label="Add a backend"], vaadin-button:has(vaadin-icon[icon$=":plus"])').last().click();
    await idle(page, 900);
    // the form of the backend, an OpenAI images API of this machine (the fake server)
    await fill(page, 'Name', 'Local images');
    await page.locator('vaadin-select').filter({ hasText: 'ComfyUI' }).first().click();
    await page.getByRole('option', { name: 'OpenAI images API', exact: true }).click();
    await idle(page, 600);
    await fill(page, 'Address', llmUrl.replace(/\/v1$/, ''));
    await fill(page, 'Model', 'marginalia-demo-image');
    await page.getByRole('checkbox', { name: 'The server is on this machine or on my network' }).check();
    await fill(page, 'Style', 'watercolor, soft light');
    await idle(page, 600);
    await page.getByText('Backends', { exact: true }).scrollIntoViewIfNeeded();
    await page.mouse.move(1100, 600);
    await page.mouse.wheel(0, 480);
    await idle(page, 700);
    await snap(page, 'plugin-imagegenerator-settings', { clip: CONTENT });
    await press(page, 'Save');
    await idle(page, 1500);
  });

  await section(page, 'generate', async () => {
    await openBook(page);
    await bookTab(page, 'Story');
    await idle(page, 800);
    const last = parts(page).last();
    await last.scrollIntoViewIfNeeded();
    await last.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
    await idle(page, 600);
    await snap(page, 'plugin-imagegenerator-menu', { clip: { x: 1580, y: 180, width: 290, height: 600 } });
    await menuItem(page, 'Generate image').click();
    await idle(page, 2500);
    await snapDialog(page, 'plugin-imagegenerator-prompt');
    await press(dialog(page), 'Generate');
    await page.getByRole('button', { name: 'Accept', exact: true }).waitFor({ timeout: 30000 });
    await idle(page, 1500);
    // the window is taller than the screen: scroll its content down to the picture
    await page.mouse.move(960, 500);
    await page.mouse.wheel(0, 900);
    await idle(page, 700);
    await snapDialog(page, 'plugin-imagegenerator');
    await press(dialog(page), 'Accept');
    await idle(page, 1500);
  });
});

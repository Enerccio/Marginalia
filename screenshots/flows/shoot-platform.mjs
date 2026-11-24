// New in 1.9.0 (run on a COPY of 13-db-backup, it changes the data): the GPUs tab of the Administration (a GPU, three steps, a lock),
// the API keys tab of the settings (a key made and its one-time display), and the phrase watch list (the settings tab and the
// story's window with a scan). Pictures: admin-gpus, gpu-step-dialog, gpu-lock-dialog, settings-api-keys, api-key-created,
// settings-phrase-watch, phrase-watch-dialog.
import {CONTENT, DESKTOP, dialog, fill, idle, login, main, menuItem, openBook, openTab, pick, press, section, sleep, snap, snapDialog} from '../lib/shot.mjs';
import {openStoryMenu} from '../lib/story.mjs';

const step = async (page, name, url, body) => {
  await press(page, 'Add step');
  const d = dialog(page);
  await fill(d, 'Name', name);
  await fill(d, 'URL', url);
  if (body) await fill(d, 'Body', body);
  await press(d, 'Save');
  await idle(page, 700);
};

await main(async ({ page }) => {
  await login(page);

  await section(page, 'gpus', async () => {
    await press(page, 'Admin');
    await idle(page, 800);
    await openTab(page, 'GPUs');
    await idle(page, 800);
    await press(page, 'Add GPU');
    await fill(dialog(page), 'Name', 'Radeon R9700');
    await press(dialog(page), 'Save');
    await idle(page, 700);

    await step(page, 'Unload the language model', 'http://litellm:4000/v1/chat/completions', '{"model": "{{model}}", "messages": []}');
    await step(page, 'Start ComfyUI', 'http://litellm:4000/gpu/comfy/start');
    await step(page, 'Stop ComfyUI', 'http://litellm:4000/gpu/comfy/stop');
    // the dialog of a step, opened again to show it
    await page.getByRole('button', { name: 'Edit' }).last().click().catch(() => {});

    await press(page, 'Add lock');
    const d = dialog(page);
    await fill(d, 'Name', 'ComfyUI images');
    await pick(d, 'GPU', 'Radeon R9700');
    await sleep(300);
    // the three lists, in order: before every job, acquire, release; each has a combo box that adds a step
    const addTo = async (index, text) => {
      const box = d.locator('vaadin-combo-box').nth(index + 1);       // the first combo box is the GPU
      await box.locator('input').click();
      await box.locator('input').fill(text);
      await page.getByRole('option', { name: text, exact: true }).first().click({ timeout: 4000 });
      await idle(page, 400);
    };
    await fill(d, 'Jobs at once', '1');
    await addTo(1, 'Start ComfyUI');
    await addTo(2, 'Stop ComfyUI');
    await sleep(300);
    await snapDialog(page, 'gpu-lock-dialog');
    await press(d, 'Save');
    await idle(page, 900);
    await page.setViewportSize({ width: DESKTOP.width, height: 1150 });
    await idle(page, 600);
    await snap(page, 'admin-gpus', { clip: { x: CONTENT.x, y: 0, width: CONTENT.width, height: 1130 } });
    await page.setViewportSize(DESKTOP);
  });

  await section(page, 'api-keys', async () => {
    await openTab(page, 'Settings');
    await page.getByText('API keys', { exact: true }).first().click();
    await idle(page, 800);
    await press(page, 'New API key');
    const d = dialog(page);
    await fill(d, 'Name', 'plugin-installer');
    await d.getByText('Manage the extensions', { exact: true }).click();
    await fill(d, 'Expires after (days)', '90');
    await sleep(300);
    await press(d, 'Create key');
    await idle(page, 900);
    await snapDialog(page, 'api-key-created');
    await press(dialog(page), 'Close');
    await idle(page, 600);
    await snap(page, 'settings-api-keys', { clip: CONTENT });
  });

  await section(page, 'phrase-watch', async () => {
    await openTab(page, 'Settings');
    await page.getByText('Phrase watch list', { exact: true }).first().click();
    await idle(page, 800);
    for (const phrase of ['a shiver ran down', 'tapestry', 'let out a breath']) {
      await press(page, 'Add phrase', { exact: false });
      await idle(page, 300);
      await page.getByPlaceholder('Phrase').last().fill(phrase);
      await idle(page, 200);
    }
    await sleep(500);
    await snap(page, 'settings-phrase-watch', { clip: CONTENT });
    await press(page, 'Save');
    await idle(page, 800);

    await openBook(page);
    await openStoryMenu(page);
    await menuItem(page, 'Phrase watch list').click();
    await idle(page, 800);
    await sleep(500);
    await snapDialog(page, 'phrase-watch-dialog');
  });
});

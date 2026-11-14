// The full-screen lorebook editor (1.8.0): the Entries tab with two entries opened in tabs beside it.
// Pictures: lorebook-fullscreen (the entries list, two entries in tabs). State: 13-db-backup (a copy is discarded, nothing is saved).
import {dialog, idle, login, main, openTab, press, section, snap} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await section(page, 'lorebook-fullscreen', async () => {
    await openTab(page, 'Lorebooks');
    await idle(page, 900);
    await page.getByRole('button', { name: 'Full-screen editor', exact: true }).click();
    await idle(page, 1500);
    const d = dialog(page);
    await page.getByRole('button', { name: 'Open in new tab' }).nth(3).click();        // Mizuki Hayashida
    await idle(page, 1200);
    await page.getByRole('button', { name: 'Open in new tab' }).nth(2).click();        // Tomáš Hájek
    await idle(page, 1500);
    await snap(page, 'lorebook-fullscreen');
    await press(d, 'Close', { wait: 600 });
  });
});

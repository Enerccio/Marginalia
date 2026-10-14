// Screenshots of the administration pages (run on step 13-db-backup or later; changes nothing).
import {dialog, idle, login, main, openTab, press, sleep, snap, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await press(page, 'Admin');
  await idle(page, 800);

  await snap(page, 'admin-users');

  await openTab(page, 'Database Backups');
  await snap(page, 'admin-database-backups');

  await openTab(page, 'Cleanup');
  await press(page, 'Analyze', { exact: false });
  await sleep(1500);
  await snap(page, 'admin-cleanup');

  // the Trash section of the Cleanup tab
  const trash = page.getByText('Trash', { exact: true }).last();
  await trash.scrollIntoViewIfNeeded();
  await trash.click();
  await sleep(1200);
  await snap(page, 'admin-trash');

  // a fresh page, so that the upload list of the extension loader is empty
  await page.reload();
  await idle(page, 1500);
  await press(page, 'Admin');
  await idle(page, 600);
  await openTab(page, 'Extensions');
  await sleep(500);
  await snap(page, 'admin-extensions');

  // the log of an extension: clear it, stop and start the extension, so that the log shows two clean lines
  // lore-generator is the 7th row
  const btn = (name, nth = 6) => page.getByRole('button', { name, exact: true }).nth(nth);
  await btn('Log').click();
  await idle(page, 600);
  await press(dialog(page), 'Clear', { exact: false });
  await press(dialog(page), 'Close');
  await btn('Stop').click();
  await idle(page, 1500);
  await press(page, 'Refresh', { exact: false });
  await idle(page, 800);
  await snap(page, '_extension-stopped');
  await btn('Start', 0).click();
  await idle(page, 2500);
  await press(page, 'Refresh', { exact: false });
  await idle(page, 800);
  await btn('Log').click();
  await idle(page, 800);
  await snapDialog(page, 'extension-log');
});

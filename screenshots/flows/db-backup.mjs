// Admin -> Database Backups: one manual backup of the installation.
import {idle, login, main, openTab, press, sleep} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await press(page, 'Admin');
  await openTab(page, 'Database Backups');
  await press(page, 'Create Backup', { exact: false });
  await sleep(2500);
  await press(page, 'Refresh', { exact: false });
  await idle(page, 800);
});

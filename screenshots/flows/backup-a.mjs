// Takes a backup of the demo book (first one: the story only, no pictures yet).
import {bookTab, login, main, openBook, press, sleep} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Backups');
  await press(page, 'Take Backup', { exact: false });
  await sleep(1500);
});

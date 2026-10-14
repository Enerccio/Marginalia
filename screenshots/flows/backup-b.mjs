// A second backup (with the picture) and the per-book backup strategy, as shown on the Backups tab.
import {bookTab, field, fill, login, main, openBook, pick, press, sleep} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Backups');
  await field(page, 'Per manuscript override').check();
  await sleep(400);
  await pick(page, 'Backup Strategy', 'After N Messages');
  await sleep(300);
  await fill(page, 'Number of messages', '7');
  await sleep(300);
  await press(page, 'Take Backup', { exact: false });
  await sleep(1500);
});

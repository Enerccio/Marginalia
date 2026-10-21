// The command line of the story editor: the list, the help, a result, a command that takes time and the question of
// the search and replace command. Pictures: command-list, command-help, command-result, command-running, command-confirm.
// State: a COPY of 13-db-backup (/take-backup adds a backup to the book; the replace is refused, so nothing else changes).
import {bookTab, idle, login, main, openBook, section, sleep, snap} from '../lib/shot.mjs';

const BAR = 'input[placeholder="Type / for commands"]';
// the lower part of the story tab: the command line with everything that opens above it
const LOWER = { x: 400, y: 330, width: 1500, height: 520 };

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  const input = page.locator(BAR);
  const clear = async () => {
    await input.click();
    await input.fill('');
    await input.press('Escape');
    await sleep(300);
  };

  await section(page, 'list', async () => {
    await input.click();
    await input.pressSequentially('/', { delay: 30 });
    await sleep(800);
    await snap(page, 'command-list', { clip: LOWER });
    await clear();
  });

  await section(page, 'help', async () => {
    await input.click();
    await input.pressSequentially('/search-replace-lore-entries Draconia ', { delay: 15 });
    await sleep(800);
    await snap(page, 'command-help', { clip: LOWER });
    await clear();
  });

  await section(page, 'result', async () => {
    await input.click();
    await input.pressSequentially('/take-backup', { delay: 15 });
    await input.press('Enter');
    await sleep(2500);
    await snap(page, 'command-result', { clip: LOWER });
    await clear();
  });

  // 'command-running' used /hello-world-async, which the application no longer has (it is a fixture of the tests); the picture
  // in the manual is the old one. It needs a command that takes time and works without a model to be retaken.

  await section(page, 'confirm', async () => {
    await input.click();
    await input.pressSequentially('/search-replace-parts Draconia Dracona', { delay: 10 });
    await input.press('Enter');
    await page.getByRole('button', { name: 'Yes', exact: true }).waitFor({ timeout: 20000 });
    await idle(page, 600);
    await page.evaluate(() => document.activeElement && document.activeElement.blur());
    await snap(page, 'command-confirm', { clip: { x: 560, y: 300, width: 800, height: 320 } });
    await page.getByRole('button', { name: 'No', exact: true }).click();
    await idle(page, 800);
  });
});

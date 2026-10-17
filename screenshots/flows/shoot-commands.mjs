// The command line of the story editor: the list, the help, a result, a command that takes time and the question of
// the search and replace command. Pictures: command-list, command-help, command-result, command-running, command-confirm.
// State: a COPY of 13-db-backup (/take-backup adds a backup to the book; the replace is refused, so nothing else changes).
import {bookTab, closeDialog, dialogRects, idle, login, main, openBook, section, sleep, snap, snapDialog} from '../lib/shot.mjs';

const BAR = 'input[placeholder="Type / for commands"]';
// the lower part of the story tab: the command line with everything that opens above it
const LOWER = { x: 400, y: 330, width: 1500, height: 550 };

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

  await section(page, 'running', async () => {
    await input.click();
    await input.pressSequentially('/hello-world-async "Hello from a worker"', { delay: 10 });
    await input.press('Enter');
    await sleep(1800);
    await snap(page, 'command-running', { clip: LOWER });
    await page.getByRole('button', { name: 'Stop', exact: true }).click();
    await idle(page, 800);
  });

  await section(page, 'confirm', async () => {
    await input.click();
    await input.pressSequentially('/search-replace-parts Draconia Dracona', { delay: 10 });
    await input.press('Enter');
    await page.locator('vaadin-confirm-dialog[opened], vaadin-dialog[opened]').filter({ hasText: 'cannot be undone' }).first().waitFor({ timeout: 20000 });
    await idle(page, 600);
    await snapDialog(page, 'command-confirm', 0);
    await page.getByRole('button', { name: 'No', exact: true }).click();
    await idle(page, 800);
  });
});

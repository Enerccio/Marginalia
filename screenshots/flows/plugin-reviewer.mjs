// Reviewer: a review of the last part (a made-up forum thread), the advanced options, the settings.
import {bookTab, closeDialog, idle, login, main, openBook, section, sleep, snapDialog} from '../lib/shot.mjs';
import {parts} from '../lib/story.mjs';

const reviewMenu = async (page, item) => {
  const last = parts(page).last();
  await last.scrollIntoViewIfNeeded();
  await last.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
  await idle(page, 400);
  await page.getByRole('menuitem', { name: 'Review' }).first().hover();
  await sleep(500);
  await page.getByRole('menuitem', { name: item, exact: false }).first().click();
};

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');

  await section(page, 'review', async () => {
    await reviewMenu(page, 'View / Generate Review');
    await page.getByText('He followed her in book six', { exact: false }).first().waitFor({ timeout: 40000 });
    await page.getByRole('button', { name: 'Stop generating' }).waitFor({ state: 'detached', timeout: 20000 }).catch(() => {});
    await sleep(800);
    await snapDialog(page, 'plugin-reviewer');
    await closeDialog(page);
  });

  await section(page, 'advanced', async () => {
    await reviewMenu(page, 'Advanced Options');
    await idle(page, 800);
    await snapDialog(page, 'plugin-reviewer-advanced');
    await closeDialog(page);
  });
});

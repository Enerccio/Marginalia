// Step 06-story -> 07-summaries: a summary of part #3 and of part #5, then a meta summary of both.
import {bookTab, dialog, idle, login, main, openBook, press, sleep, snapDialog} from '../lib/shot.mjs';
import {partAction, clickSummaries} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');

  // a summary of #3 (covers #1-#3) - the result is shown in a dialog
  await partAction(page, 2, 'Generate summary');
  await sleep(3500);
  await snapDialog(page, 'summary-dialog');
  await press(dialog(page), 'Exit');

  // a summary of #5 (covers #4-#5)
  await partAction(page, -1, 'Generate summary');
  await sleep(3500);
  await press(dialog(page), 'Exit');

  // the Summaries dialog (list button of the story editor)
  await clickSummaries(page);
  await idle(page, 800);
  await snapDialog(page, '_summaries-plain');

  // meta summary of both: select the rows, create; the answer shows in a Summary dialog over the Summaries dialog
  await page.mouse.click(1614, 320);
  await page.mouse.click(1614, 479);
  await sleep(300);
  await page.mouse.click(1640, 149);
  await sleep(3500);
  await snapDialog(page, 'meta-summary-create', 1);
  await press(dialog(page), 'Exit');
  await idle(page, 800);
  await snapDialog(page, 'summaries-dialog');

  // edit mode of a summary (pen -> check/trash); leave it with the check, nothing is changed
  await page.locator('vaadin-dialog[opened] vaadin-icon[icon="fas:pen"]').first().click();
  await idle(page, 500);
  await snapDialog(page, 'summary-edit');
  await page.locator('vaadin-dialog[opened] vaadin-icon[icon="fas:check"]').first().click();
  await idle(page, 600);

  // deleting a meta summary asks: Unwind / Delete / Cancel
  await page.locator('vaadin-grid vaadin-button[title="Delete"]').first().click();
  await idle(page, 600);
  await snapDialog(page, 'meta-summary-delete');
  await press(dialog(page), 'Cancel');
});

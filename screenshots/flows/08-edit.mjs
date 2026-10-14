// Step 08-images -> 09-edit: the user edits a generated part (so that "Highlight Generated Text" has something to show).
import {bookTab, idle, login, main, openBook, snap} from '../lib/shot.mjs';
import {partAction, parts} from '../lib/story.mjs';
import {edit} from '../dataset/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await partAction(page, -1, 'Edit');
  await idle(page, 600);
  const area = parts(page).last().locator('textarea').first();
  const text = await area.inputValue();
  if (!text.includes(edit.from)) throw new Error('text to edit not found');
  await area.fill(text.replace(edit.from, edit.to));
  await area.dispatchEvent('change');
  await partAction(page, -1, 'Save');
  await idle(page, 1000);
  await snap(page, '_edited');

  // highlight on, menu open to show the checked item
  await page.mouse.click(534, 822);
  await idle(page, 400);
  await page.getByText('Highlight Generated Text', { exact: true }).click();
  await idle(page, 800);
  await page.mouse.click(534, 822);
  await idle(page, 400);
  await snap(page, '_highlight');
});

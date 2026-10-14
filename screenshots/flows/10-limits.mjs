// Step 10-users -> 11-limits: a token limit of the regular user on the shared provider; usage and the user's info.
import {fillTurn, generate} from '../lib/story.mjs';
import {ginnyBook, ginnyTurn2} from '../dataset/story.mjs';
import {
    bookTab,
    CONTENT,
    dialog,
    field,
    fill,
    idle,
    login,
    logout,
    main,
    openBook,
    openTab,
    press,
    sleep,
    snap,
    snapDialog,
    users
} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openTab(page, 'Inference Providers');
  await page.mouse.click(1792, 228);                      // Limits of the shared provider (second row)
  await idle(page, 800);
  await press(page, 'Add limit', { exact: false });
  await idle(page, 600);
  let d = dialog(page);
  await field(d, 'User').click();
  await sleep(300);
  await page.getByRole('option', { name: users.reader.login, exact: true }).click();
  await field(d, 'Reset').click();
  console.log('reset options:', (await page.getByRole('option').allInnerTexts()).join(' | '));
  await page.getByRole('option').filter({ hasText: 'Weekly' }).first().click();
  await fill(d, 'Limit of tokens sent', '200000');
  await fill(d, 'Limit of tokens generated', '40000');
  await fill(d, 'Limit of tokens in total', '220000');
  await sleep(300);
  await snapDialog(page, 'provider-limit-edit');
  await press(d, 'Save');
  await idle(page, 800);
  await snapDialog(page, 'provider-limits');
  await press(dialog(page), 'Close');
  await idle(page, 500);

  // what every user has used
  await press(page, 'Usage per user', { exact: false });
  await idle(page, 800);
  await snapDialog(page, 'provider-usage');
  await press(dialog(page), 'Close');
  await logout(page);

  // the user's side: shared provider as a read-only row, the info dialog
  await login(page, users.reader);
  // she writes a second part, so that the limit has numbers
  await openBook(page, ginnyBook);
  await bookTab(page, 'Story');
  await fillTurn(page, ginnyTurn2);
  await generate(page, ginnyTurn2);
  await press(page, 'Exit');
  await idle(page, 800);
  await openTab(page, 'Inference Providers');
  await snap(page, 'provider-shared-user', { clip: CONTENT });
  await page.locator('vaadin-grid-cell-content vaadin-button').first().click();   // the info button of the shared provider
  await idle(page, 800);
  await snapDialog(page, 'provider-limit-info');
  await press(dialog(page), 'Close');
});

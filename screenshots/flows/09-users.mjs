// Step 09-edit -> 10-users: a regular user who writes a short book with the shared provider.
import {
    bookTab,
    dialog,
    field,
    fill,
    idle,
    login,
    logout,
    main,
    openTab,
    pick,
    press,
    sleep,
    snap,
    users
} from '../lib/shot.mjs';
import {fillTurn, generate} from '../lib/story.mjs';
import {ginnyBook, ginnyTurn} from '../dataset/story.mjs';

await main(async ({ page }) => {
  // the administrator adds the user
  await login(page);
  await press(page, 'Admin');
  await press(page, 'Add User');
  const d = dialog(page);
  await fill(d, 'Username', users.reader.login);
  await fill(d, 'Full Name', users.reader.name);
  await fill(d, 'Password', users.reader.password);
  await fill(d, 'Repeat password', users.reader.password);
  await press(d, 'OK');
  await idle(page, 800);
  await snap(page, '_users');
  await logout(page);

  // the user: her own protocol and defaults (protocols are per user, only the provider is shared)
  await login(page, users.reader);
  await openTab(page, 'Inference Providers');
  await snap(page, '_reader-providers');
  await openTab(page, 'Protocols');
  await press(page, 'Add Protocol');
  {
    const p = dialog(page);
    await fill(p, 'Name', 'Chat Completion');
    await field(p, 'Enable Temperature').check();
    await fill(p, 'Temperature', '0.9');
    await press(p, 'OK');
  }
  await openTab(page, 'Settings');
  await pick(page, 'Default Model', 'Local LLM');
  await pick(page, 'Default Protocol', 'Chat Completion');
  await press(page, 'Save');
  await sleep(500);

  // and writes one part with the shared provider
  await openTab(page, 'Books');
  await press(page, 'Add Book');
  await dialog(page).locator('input').first().fill(ginnyBook);
  await press(dialog(page), 'Yes');
  await page.getByText('Branch view', { exact: true }).first().waitFor();
  await idle(page, 1000);
  await bookTab(page, 'Story');
  await fillTurn(page, ginnyTurn);
  await generate(page, ginnyTurn);
  await snap(page, '_reader-story');
});

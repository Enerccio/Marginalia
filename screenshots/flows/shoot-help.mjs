// The help assistant: its tab in Admin and the chat panel (run on a copy of 13-db-backup, it is discarded: the settings are
// installation wide and the administrator gets a permission). The fake model answers the question from the queue; the
// answer is what the manual says about restoring.
import {addPermission, idle, llm, login, main, openPermissions, openTab, pick, press, sleep, snap} from '../lib/shot.mjs';

const ANSWER = `On the **Backups** tab of the book, click **Restore** next to the backup you want to go back to, then choose **Full Restore** (everything) or **Messages Only Restore** (only the parts and summaries). Restoring replaces **all parts** of the book, so take a backup of the current state first.

*Pages used: Book backups (books/backups.md).*`;

await main(async ({ page }) => {
  await llm('/_reset', {});
  await login(page);

  // the settings of the administrator
  await page.getByRole('button', { name: 'Admin', exact: true }).first().click();
  await idle(page, 800);
  await openTab(page, 'Help assistant');
  const panel = page.getByRole('tabpanel').filter({ hasText: 'Reset to default' });
  await pick(panel, 'Model', 'Local LLM');
  await sleep(400);
  await snap(page, 'admin-help-assistant');
  await press(panel, 'Save');
  await idle(page, 800);

  // what depends on the user is in their permission: the administrator asks without one, but the assistant moves through
  // the interface only for those whose permission allows it, so they give it to themselves (the page is not reloaded)
  await openTab(page, 'Users');
  const d = await openPermissions(page, 'admin');
  await addPermission(d, 'Help assistant');
  await d.getByText('Allow navigating the interface', { exact: true }).click();
  await sleep(300);
  await press(d, 'Save');
  await idle(page, 800);
  await press(d, 'Exit');
  await idle(page, 500);

  // the button is there when the page is built: reload, then ask
  await page.reload();
  await idle(page, 1500);
  await page.locator('.help-fab').click();
  await idle(page, 800);
  await llm('/_queue', { reply: ANSWER });
  const ask = page.locator('.help-panel input[type="text"]');
  await ask.fill('How do I go back to an earlier state of my story?');
  await ask.press('Enter');
  await page.locator('.help-message').getByText('Pages used').waitFor();
  await idle(page, 1200);
  await snap(page, 'help-assistant', { locator: page.locator('.help-panel'), pad: 10 });

  // "show me": the model reads the screen, then opens the Admin page and selects the tab (what the real model would call)
  await llm('/_queue', { toolCalls: [{ name: 'get_ui_state', arguments: { path: '' } }] });
  await llm('/_queue', { toolCalls: [{ name: 'interact_with_ui', arguments: {
    path: 'UI/Main/DesktopWorkspace', interactions: ['open-section:Admin'] } }] });
  await llm('/_queue', { toolCalls: [{ name: 'interact_with_ui', arguments: {
    path: 'UI/Main/DesktopWorkspace/AdminPart', interactions: ['open-tab:Database Backups'] } }] });
  await llm('/_queue', { reply: 'I opened the **Admin** page and selected the **Database Backups** tab. **Take Backup** makes a manual backup of the whole database.\n\n*Pages used: Database backups (administration/database-backups.md).*' });
  const ask2 = page.locator('.help-panel input[type="text"]');
  await ask2.fill('Show me where the database backups are');
  await ask2.press('Enter');
  await page.locator('.help-message').getByText('I opened the').waitFor();
  await idle(page, 1500);
  await snap(page, 'help-assistant-navigate');

  // a form that can not be saved: the assistant stays usable above the dialog, reads what is wrong with the form
  // (get_ui_state lists the empty required field) and explains it
  await page.locator('.help-panel').getByRole('button').first().click(); // the broom: a new conversation
  await idle(page, 500);
  await openTab(page, 'Inference Providers');
  await page.getByRole('button', { name: 'Add Inference Provider' }).click();
  await idle(page, 900);
  await llm('/_queue', { toolCalls: [{ name: 'get_ui_state', arguments: { path: 'UI/AIDialog' } }] });
  await llm('/_queue', { reply: 'The form can not be saved yet: **Name** is required and still empty. Give the provider a name (for example "Local LLM"), then fill in the address and model on the **Per Type Settings** tab and press **OK**.\n\n*Pages used: [Inference providers](https://enerccio.github.io/Marginalia/user/inference-providers/).*' });
  const ask3 = page.locator('.help-panel input[type="text"]');
  await ask3.fill('Why can I not save this?');
  await ask3.press('Enter');
  await page.locator('.help-message').getByText('still empty').waitFor();
  await idle(page, 1500);
  await snap(page, 'help-assistant-form');
});

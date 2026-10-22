// The permissions window of a user (Admin → Users, the key button): adding a permission, the settings of one, the list.
// Run on a copy of 13-db-backup, it is discarded: the permissions of ginny are changed (the migration of the application
// gave her "Create own models", as everybody could create models before the permissions). Pictures: permissions-add,
// permissions-settings, permissions-settings-agents, permissions-dialog.
import {addPermission, dialogRects, idle, login, main, openPermissions, press, sleep, snap, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await press(page, 'Admin');
  await idle(page, 800);

  const d = await openPermissions(page, 'ginny');

  // the combo box offers what she has none of
  await d.getByLabel('Permission', { exact: true }).click();
  await sleep(500);
  // (snapDialog would take the focus off the combo box and close the list)
  await snap(page, 'permissions-add', { clip: (await dialogRects(page))[0] });
  await page.keyboard.press('Escape');

  // a permission with settings is added unfolded
  await addPermission(d, 'Help assistant');
  await d.getByText('Allow navigating the interface', { exact: true }).click();
  await sleep(400);
  await snapDialog(page, 'permissions-settings');

  // the third permission, with the limits of the agents (unfolded too; the first one is folded to make room)
  await d.locator('vaadin-button[theme~="tertiary-inline"]').first().click();
  await sleep(300);
  await addPermission(d, 'Agents');
  // nine fields: the window is made taller than the usual 932 pixels so that none of them has to be scrolled to
  await page.setViewportSize({ width: 1920, height: 1500 });
  await page.evaluate(() => { [...document.querySelectorAll('vaadin-dialog[opened]')].pop().height = '1250px'; });
  await d.locator('vaadin-grid').first().evaluate(grid => { grid.style.height = '1020px'; });
  await sleep(900);
  await snapDialog(page, 'permissions-settings-agents');
  await page.evaluate(() => { [...document.querySelectorAll('vaadin-dialog[opened]')].pop().height = null; });
  await d.locator('vaadin-grid').first().evaluate(grid => { grid.style.height = ''; });
  await page.setViewportSize({ width: 1920, height: 932 });
  await sleep(500);

  // saved, then folded: the list
  await press(d, 'Save');
  await idle(page, 800);
  await d.locator('vaadin-button[theme~="tertiary-inline"]').nth(1).click();
  await sleep(500);
  await snapDialog(page, 'permissions-dialog');
});


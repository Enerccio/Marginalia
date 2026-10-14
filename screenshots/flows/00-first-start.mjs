// Fresh installation (step 00-empty): the administrator dialog, then the login card. Result: step 01-admin.
import {fill, goto, idle, launch, snap, users} from '../lib/shot.mjs';

const { browser, page } = await launch();
await goto(page, '/');
await fill(page, 'Username', users.admin.login);
await fill(page, 'Full Name', users.admin.name);
await fill(page, 'Password', users.admin.password);
await fill(page, 'Repeat password', users.admin.password);
await page.locator('body').click({ position: { x: 10, y: 10 } });
await snap(page, 'first-start');
await page.getByRole('button', { name: 'OK' }).click();
await idle(page, 1500);
await page.locator('body').click({ position: { x: 10, y: 10 } });
await page.evaluate(() => document.activeElement && document.activeElement.blur());
const card = page.locator('vaadin-login-form-wrapper, vaadin-login-form').first();
await snap(page, 'login', { clip: { x: 778, y: 203, width: 364, height: 527 } });
await browser.close();

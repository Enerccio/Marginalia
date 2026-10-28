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
// the version and commit under the button (the build detail that the footer of the other pictures hides) is left out
await page.evaluate(() => {
  const walk = root => {
    for (const el of root.querySelectorAll('*')) {
      if (el.shadowRoot) walk(el.shadowRoot);
      if (el.children.length === 0 && /^\s*Marginalia \d+\.\d+/.test(el.textContent)) el.style.display = 'none';
    }
  };
  walk(document);
});
await idle(page, 400);
const card = page.locator('vaadin-login-form-wrapper').first();
const box = await card.boundingBox();
const brand = 172;                                    // the blue header (the title and the slogan) above the form
await snap(page, 'login', { clip: { x: box.x, y: box.y - brand, width: box.width, height: box.height + brand } });
await browser.close();

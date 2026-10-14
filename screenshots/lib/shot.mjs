// Helpers shared by the screenshot flows (Playwright driving the system Google Chrome, headless).
import {chromium} from 'playwright-core';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

export const root = path.join(path.dirname(fileURLToPath(import.meta.url)), '..');
export const repo = path.join(root, '..');
export const outDir = process.env.SHOT_OUT || path.join(root, 'out');
export const baseUrl = process.env.SHOT_URL || 'http://localhost:8099';
export const llmUrl = `http://localhost:${process.env.SHOT_LLM_PORT || 8098}/v1`;
export const sleep = ms => new Promise(r => setTimeout(r, ms));

export const DESKTOP = { width: 1920, height: 932 };   // all full window shots
export const PHONE = { width: 402, height: 874 };      // phone shots

export const users = {
  admin: { login: 'admin', name: 'Administrator', password: 'marginalia' },
  reader: { login: 'ginny', name: 'Ginny W.', password: 'marginalia' },
};

/** Opens the browser. `phone` emulates a phone (touch, mobile user agent) in Chrome. */
export async function launch({ phone = false, locale = 'en-US', scale = 1 } = {}) {
  const browser = await chromium.launch({ channel: 'chrome', headless: true, args: ['--hide-scrollbars'] });
  const context = await browser.newContext({
    viewport: phone ? PHONE : DESKTOP,
    deviceScaleFactor: scale,
    colorScheme: 'dark',
    locale,
    isMobile: phone,
    hasTouch: phone,
    userAgent: phone
      ? 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1'
      : undefined,
  });
  const page = await context.newPage();
  page.setDefaultTimeout(15000);
  // toasts ("Settings saved...") must not end up in a screenshot
  if (!process.env.SHOT_TOASTS) await page.addInitScript(() => {
    const css = 'vaadin-notification-card, vaadin-notification-container { display: none !important; }';
    const add = () => { const st = document.createElement('style'); st.id = 'shot-hide-toasts'; st.textContent = css; (document.head || document.documentElement).appendChild(st); };
    if (document.head) add(); else document.addEventListener('DOMContentLoaded', add);
  });
  page.on('pageerror', e => console.log('  [pageerror]', String(e).slice(0, 200)));
  return { browser, context, page };
}

/** Waits until the Vaadin client has no request in flight and nothing is loading. */
export async function idle(page, extra = 250) {
  await page.waitForFunction(() => {
    const clients = window.Vaadin && window.Vaadin.Flow && window.Vaadin.Flow.clients;
    if (!clients) return true;
    return Object.values(clients).every(c => !c.isActive || !c.isActive());
  }, null, { timeout: 30000 }).catch(() => {});
  await sleep(extra);
}

export async function goto(page, p = '/') {
  await page.goto(baseUrl + p, { waitUntil: 'load' });
  await idle(page, 600);
}

/** The input of the Vaadin field (text, number, area, password, combo box, checkbox...) with the given label. */
export function field(scope, label) {
  return scope.getByLabel(label, { exact: true }).and(scope.locator('input, textarea')).first();
}
/** Types into the Vaadin field with the given label (inside `scope`, a page or locator). */
export async function fill(scope, label, value) {
  const input = field(scope, label);
  if (await input.getAttribute('type') === 'password') {
    // a password field only syncs from real key presses followed by a blur
    await input.click();
    await input.pressSequentially(value, { delay: 15 });
    await input.press('Tab');
  } else {
    await input.fill(value);
    await input.dispatchEvent('change');
  }
}

/** Picks an item of a combo box: types its text, clicks the matching option (or confirms a custom value with Enter). */
export async function pick(scope, label, text) {
  const input = field(scope, label);
  await input.click();
  await input.fill(text);
  const page = scope.page ? scope.page() : scope;
  const option = page.getByRole('option', { name: text, exact: true }).first();
  try {
    await option.click({ timeout: 2500 });
  } catch {
    await input.press('Enter');
  }
  await sleep(250);
}

export async function login(page, who = users.admin, path = '/') {
  await goto(page, path);
  const user = page.getByLabel('Username', { exact: true });
  if (await user.count() === 0) return;           // already logged in
  await user.fill(who.login);
  await page.getByLabel('Password', { exact: true }).fill(who.password);
  await page.getByRole('button', { name: 'Login', exact: true }).click();
  await idle(page, 1000);
  await dismissLoginError(page);
}

export async function logout(page) {
  await page.getByRole('button', { name: 'Logout' }).click();
  await idle(page, 800);
}

/** Saves a screenshot as out/<name>.png. `clip` {x,y,width,height} or `locator` crops. */
export async function snap(page, name, { clip, locator, pad = 0 } = {}) {
  fs.mkdirSync(outDir, { recursive: true });
  const file = path.join(outDir, name + '.png');
  await idle(page, 300);
  if (locator) {
    const box = await locator.boundingBox();
    await page.screenshot({ path: file, clip: {
      x: Math.max(0, box.x - pad), y: Math.max(0, box.y - pad),
      width: box.width + 2 * pad, height: box.height + 2 * pad } });
  } else {
    await page.screenshot({ path: file, clip });
  }
  console.log('  saved', path.relative(repo, file));
  return file;
}

/** Resets the fake inference server between flows. */
export async function llm(pathname, body) {
  const res = await fetch(llmUrl + pathname, {
    method: body === undefined ? 'GET' : 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  return res.json();
}

export function arg(name, fallback) {
  const i = process.argv.indexOf('--' + name);
  return i >= 0 ? (process.argv[i + 1] ?? true) : fallback;
}

/** The topmost open dialog (a Vaadin 25 `vaadin-dialog`/`vaadin-confirm-dialog`; its content is light DOM). */
export function dialog(page) {
  return page.locator('vaadin-dialog[opened], vaadin-confirm-dialog[opened]').last();
}
/** The visible card of the topmost dialog (for cropping). */
export function dialogBox(page, which = -1) {
  if (typeof which === 'string') {
    // the dialog that contains this text (when dialogs are stacked)
    return page.locator('vaadin-dialog[opened], vaadin-confirm-dialog[opened]').filter({ hasText: which }).last().locator('[part="overlay"]').first();
  }
  return page.locator('vaadin-dialog[opened] [part="overlay"], vaadin-confirm-dialog[opened] [part="overlay"]').nth(which);
}
export async function openTab(page, name) {
  await page.getByText(name, { exact: true }).first().click();
  await idle(page, 700);
}
/** Clicks a button by its visible text (first match, or inside `scope`). */
export async function press(scope, name, opts = {}) {
  await scope.getByRole('button', { name, exact: opts.exact ?? true }).first().click();
  await idle(page_of(scope), opts.wait ?? 600);
}
function page_of(scope) { return scope.page ? scope.page() : scope; }
/** Full content area crop used by the "tab" screenshots (workspace without the left navigation). */
export const CONTENT = { x: 292, y: 15, width: 1612, height: 900 };

/**
 * Runs a flow: opens the browser, calls fn({browser, page, context}), prints a short error (and saves
 * out/_fail.png) instead of Playwright's long call log, always closes the browser.
 */
export async function main(fn, opts = {}) {
  const session = await launch(opts);
  try {
    await fn(session);
  } catch (e) {
    console.log('FAILED:', String(e.message || e).split('\n').slice(0, 4).join('\n  '));
    await session.page.screenshot({ path: path.join(outDir, '_fail.png') }).catch(() => {});
    process.exitCode = 1;
  } finally {
    await session.browser.close();
  }
}

/** Opens a book from the Books tab (the pen button of its row) and waits for the book window. */
export async function openBook(page, name = 'Harry Potter and Half Blood Prince - FDM') {
  await openTab(page, 'Books');
  const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: name }).first();
  const box = await cell.boundingBox();
  // the pen button is at the right end of the row of the name cell
  await page.mouse.click(1804, box.y + box.height / 2);
  await page.getByText('Branch view', { exact: true }).first().waitFor();
  await idle(page, 800);
}
export async function bookTab(page, name) {
  await page.getByRole('tab', { name, exact: true }).first().click().catch(async () => {
    await page.getByText(name, { exact: true }).first().click();
  });
  await idle(page, 800);
}

/** Rectangles of all open dialog cards, smallest first (the book window is a dialog too, it is the biggest). */
export async function dialogRects(page) {
  return page.evaluate(() => {
    const out = [];
    for (const d of document.querySelectorAll('vaadin-dialog[opened], vaadin-confirm-dialog[opened]')) {
      const root = d.shadowRoot && d.shadowRoot.querySelector('vaadin-dialog-overlay, vaadin-confirm-dialog-overlay');
      const part = root && root.shadowRoot && root.shadowRoot.querySelector('[part="overlay"]');
      const r = (part || d).getBoundingClientRect();
      if (r.width > 0 && r.height > 0) out.push({ x: r.x, y: r.y, width: r.width, height: r.height });
    }
    return out.sort((a, b) => a.width * a.height - b.width * b.height);
  });
}

/** Screenshot of the n-th smallest open dialog (0 = the one on top in the usual case). */
export async function snapDialog(page, name, rank = 0, pad = 0) {
  await page.evaluate(() => document.activeElement && document.activeElement.blur && document.activeElement.blur());
  const rects = await dialogRects(page);
  const r = rects[rank < 0 ? rects.length + rank : rank];
  if (!r) throw new Error(`no dialog of rank ${rank} (found ${rects.length}) for ${name}`);
  return snap(page, name, { clip: { x: r.x - pad, y: r.y - pad, width: r.width + 2 * pad, height: r.height + 2 * pad } });
}

/**
 * A regular user gets an "Application Error" (SecurityException of the admin part, TODO.BUGS.md BUG-63) right after
 * the login. Closes it so that the flows can go on.
 */
export async function dismissLoginError(page) {
  for (let i = 0; i < 6; i++) {
    await sleep(700);
    if (!(await page.getByText('Application Error', { exact: true }).count())) return;
    console.log('  (dismissed an Application Error dialog after the login, see BUG-63)');
    await page.locator('vaadin-dialog[opened]').filter({ hasText: 'Application Error' }).last().getByRole('button', { name: 'Exit', exact: true }).click();
    await idle(page, 400);
  }
}

/** Runs one part of a shooting script; a failure is reported and the next part goes on (the page is left as it is). */
export async function section(page, name, fn) {
  const only = (arg('only', process.env.SHOT_ONLY || '') + '').split(',').filter(Boolean);
  if (only.length && !only.includes(name)) return;
  try {
    await fn();
    console.log('  ok  ', name);
  } catch (e) {
    console.log('  FAIL', name, '-', String(e.message || e).split('\n')[0].slice(0, 160));
    await page.screenshot({ path: path.join(outDir, '_fail-' + name.replace(/[^a-z0-9]+/gi, '-') + '.png') }).catch(() => {});
    // close dialogs that may be left open, so the next part starts clean
    for (let i = 0; i < 3; i++) {
      const open = await page.locator('vaadin-dialog[opened], vaadin-confirm-dialog[opened]').count();
      if (!open) break;
      const b = page.locator('vaadin-dialog[opened], vaadin-confirm-dialog[opened]').last().getByRole('button', { name: /^(Cancel|Close|Exit|No)$/ }).first();
      if (await b.count()) await b.click().catch(() => {}); else break;
      await idle(page, 300);
    }
  }
}

/** Rectangles of the open popovers (hints, turn details...), smallest first. */
export async function popoverRects(page) {
  return page.evaluate(() => {
    const out = [];
    for (const p of document.querySelectorAll('vaadin-popover')) {
      const ov = p.shadowRoot && p.shadowRoot.querySelector('vaadin-popover-overlay');
      const part = ov && ov.shadowRoot && ov.shadowRoot.querySelector('[part="overlay"]');
      const r = part && part.getBoundingClientRect();
      if (r && r.width > 20 && r.height > 20) out.push({ x: r.x, y: r.y, width: r.width, height: r.height });
    }
    return out.sort((a, b) => a.width * a.height - b.width * b.height);
  });
}

/** Screenshot of the n-th smallest open popover. */
export async function snapPopover(page, name, rank = 0) {
  const rects = await popoverRects(page);
  const r = rects[rank < 0 ? rects.length + rank : rank];
  if (!r) throw new Error(`no popover for ${name}`);
  return snap(page, name, { clip: r });
}

/** Closes the topmost dialog with whichever of OK / Close / Exit / Cancel / No it has. */
export async function closeDialog(page) {
  const d = dialog(page);
  for (const name of ['Close', 'OK', 'Exit', 'Cancel', 'No']) {
    const b = d.getByRole('button', { name, exact: true });
    if (await b.count()) { await b.first().click(); await idle(page, 400); return; }
  }
  await page.keyboard.press('Escape');
}

/** Picks an item of a Vaadin Select (not a combo box): opens it and clicks the item with this text. */
export async function pickSelect(scope, text) {
  const page = scope.page ? scope.page() : scope;
  await scope.locator('vaadin-select').first().click();
  await sleep(400);
  await page.locator('vaadin-select-item').filter({ hasText: text }).first().click();
  await sleep(300);
}

/** Lets the toasts (notifications) be seen again: they are hidden in all other screenshots. */
export async function showToasts(page) {
  await page.evaluate(() => { const st = document.getElementById('shot-hide-toasts'); if (st) st.remove(); });
}

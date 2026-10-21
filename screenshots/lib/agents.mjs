// Helpers of the flows that drive the Agents tab (the buttons of a row are icons with a tooltip, not words).
import {dialog, idle, llm, openTab, pick, fill, press, sleep, snap} from './shot.mjs';

/** The Y of the row of the agent in the table of the Agents tab. */
async function rowY(page, name) {
  const cell = page.locator('vaadin-grid-cell-content').filter({ hasText: new RegExp('^\\s*' + name + '\\s*$') }).first();
  return (await cell.boundingBox()).y;
}

/** The button with this icon (`play`, `terminal`, `pen`, `clone`, `trash`, `stop`, `pause`, `bolt`, `code-compare`) in the row of the agent. */
export async function rowButton(page, name, icon) {
  const y = await rowY(page, name);
  const buttons = page.locator(`vaadin-grid vaadin-button:has(vaadin-icon[icon$=":${icon}"]), vaadin-grid vaadin-menu-bar-button:has(vaadin-icon[icon$=":${icon}"])`);
  let best = null;
  for (let i = 0; i < await buttons.count(); i++) {
    const box = await buttons.nth(i).boundingBox();
    if (!box) continue;
    const distance = Math.abs(box.y - y);
    if (distance < 30 && (best === null || distance < best.distance)) best = { index: i, distance };
  }
  if (!best) throw new Error(`no ${icon} button in the row of ${name}`);
  return buttons.nth(best.index);
}

export async function clickRow(page, name, icon, wait = 800) {
  await (await rowButton(page, name, icon)).click();
  await idle(page, wait);
}

/** Starts the agent from the menu of its Start button: 'Start immediately' or 'Start in console'. */
export async function startAgent(page, name, how = 'Start immediately') {
  await clickRow(page, name, 'play', 500);
  await page.locator('vaadin-menu-bar-item, vaadin-context-menu-item').filter({ hasText: how }).first().click();
  await idle(page, 800);
}

/** Creates an agent in the New agent window (the book is picked when it is not the first of the list). */
export async function newAgent(page, { name, book, model = 'Local LLM', protocol = 'Chat Completion', extra } = {}) {
  await press(page, 'New agent');
  const d = dialog(page);
  await fill(d, 'Name', name);
  if (book) await pick(d, 'Book', book);
  await pick(d, 'Model', model);
  await pick(d, 'Protocols', protocol);
  if (extra) await extra(d);
  return d;
}

/** The text of the status column of the row. */
export async function statusOf(page, name) {
  const y = await rowY(page, name);
  const cells = page.locator('vaadin-grid-cell-content');
  for (let i = 0; i < await cells.count(); i++) {
    const box = await cells.nth(i).boundingBox();
    if (box && Math.abs(box.y - y) < 4 && box.x > 1150 && box.x < 1400) return (await cells.nth(i).innerText()).trim();
  }
  return '';
}

/** Waits until the status column of the agent says (contains) this text. */
export async function waitStatus(page, name, text, ms = 60000) {
  const t0 = Date.now();
  while (Date.now() - t0 < ms) {
    const s = await statusOf(page, name);
    if (s.includes(text)) return s;
    await sleep(700);
  }
  throw new Error(`status of ${name} is "${await statusOf(page, name)}", expected "${text}"`);
}

/** Queues what the fake model answers in the next rounds: each item is what `/_queue` takes ({reply, reasoning, toolCalls, delayMs}). */
export async function queueRounds(rounds) {
  for (const r of rounds) await llm('/_queue', r);
}

/** Polls the text of the topmost dialog until it contains `text`. */
export async function waitDialogText(page, text, ms = 60000) {
  const t0 = Date.now();
  while (Date.now() - t0 < ms) {
    const t = await dialog(page).innerText().catch(() => '');
    if (t.includes(text)) return t;
    await sleep(600);
  }
  throw new Error(`the dialog never said "${text}"`);
}

/** Writes into the message box of the console and presses Send. */
export async function consoleSend(page, text, wait = 800) {
  const d = dialog(page);
  await d.locator('textarea').last().fill(text);
  await d.getByRole('button', { name: 'Send', exact: true }).first().click();
  await idle(page, wait);
}

/** Selects a tab of the console (Run, Runs, Workspace, Checkpoints, Context, Book copy, Sub-agents, Diagnostics). */
export async function consoleTab(page, name, wait = 1500) {
  await dialog(page).getByRole('tab', { name, exact: true }).click();
  await idle(page, wait);
}

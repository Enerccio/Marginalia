// Driving the story editor: writing a turn, selecting parts.
import {fill, idle, press} from './shot.mjs';

/** Clicks the round "+" button of the story editor (bottom right) and fills the New Turn panel. */
export async function fillTurn(page, t) {
  await page.mouse.click(1857, 822);
  await idle(page, 600);
  await fill(page, 'Scene Setting', t.scene);
  await fill(page, 'POV Character', t.pov);
  await fill(page, 'Present Characters', t.present);
  await fill(page, 'Instructions', t.instructions);
}

/** Generates and waits until the end of the reply appears on the page. */
export async function generate(page, t, { waitFor = null, timeout = 60000 } = {}) {
  await press(page, 'Generate', { exact: false, wait: 300 });
  const tail = (waitFor ?? t.reply.trim().split('\n').pop()).slice(-40);
  await page.getByText(tail, { exact: false }).first().waitFor({ timeout });
  await idle(page, 1500);
}

/** The `.chat-message` elements (parts) of the story editor. */
export const parts = page => page.locator('.chat-message');

/** Opens the menu of the n-th part (0 based, negative counts from the end) and clicks the item. */
export async function partAction(page, index, item) {
  const all = parts(page);
  const n = await all.count();
  const part = all.nth(index < 0 ? n + index : index);
  await part.scrollIntoViewIfNeeded();
  await part.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
  await idle(page, 400);
  await page.getByRole('menuitem', { name: item, exact: true }).first().click();
  await idle(page, 600);
}

/** Switches the story text to the serif reading style (the menu item toggles it) when it is not on yet. */
export async function readingStyle(page, on = true) {
  const isOn = await page.evaluate(() => {
    const p = document.querySelector('.chat-message-content');
    if (!p) return null;
    return /garamond|georgia|serif/i.test(getComputedStyle(p.querySelector('vaadin-markdown, p, div') ?? p).fontFamily);
  });
  if (isOn === on) return;
  await page.mouse.click(534, 822);
  await idle(page, 400);
  await page.getByText('Change Styles For Text', { exact: true }).click();
  await idle(page, 500);
}

/** Drags the divider between the sidebar and the story to the right, so that all tabs of the sidebar fit. */
export async function widenSidebar(page, to = 700) {
  await page.mouse.move(405, 488);
  await page.mouse.down();
  await page.mouse.move((405 + to) / 2, 488, { steps: 5 });
  await page.mouse.move(to, 488, { steps: 5 });
  await page.mouse.up();
  await idle(page, 600);
}

/** Opens a tab of the sidebar of the story editor (Outline, Author's Note, Characters, Side Query...). */
export async function sideTab(page, name) {
  await page.getByRole('tab', { name, exact: true }).first().click();
  await idle(page, 700);
}

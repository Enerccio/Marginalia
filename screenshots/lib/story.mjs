// Driving the story editor: writing a turn, selecting parts.
import {fill, idle, menuItem, press} from './shot.mjs';

/** The buttons of the bottom bar of the story: found by their icons, their place depends on the layout of the dock. */
export const bottomButton = (page, icon) => page.locator(`vaadin-button:has(vaadin-icon[icon="${icon}"]), vaadin-menu-bar-button:has(vaadin-icon[icon="${icon}"])`).last();
/** Clicks the "+" button of the bottom bar (opens the New Turn panel). */
export async function clickPlus(page) {
  await bottomButton(page, 'fas:plus').click();
  await idle(page, 600);
}
/** Clicks the cogs button (the settings menu) of the bottom bar. */
export async function clickCogs(page) {
  await bottomButton(page, 'fas:cogs').click();
  await idle(page, 400);
}
/** Clicks the Summaries button (the list icon) of the bottom bar. */
export async function clickSummaries(page) {
  await bottomButton(page, 'fas:list-ul').click();
  await idle(page, 400);
}

/** Clicks the "+" button of the story editor (bottom right) and fills the New Turn panel. */
export async function fillTurn(page, t) {
  await clickPlus(page);
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
  await part.locator('.chat-message-content > vaadin-horizontal-layout > vaadin-horizontal-layout > vaadin-button.margin-left-auto').first().click();
  await idle(page, 400);
  await menuItem(page, item).click();
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
  await clickCogs(page);
  await menuItem(page, 'Change Styles For Text').click();
  await idle(page, 500);
}

/** Drags the divider between the sidebar and the story to the right, so that all tabs of the sidebar fit. */
export async function widenSidebar(page, to = 700) {
  // the position of the divider is saved with the book, so it is not at 405 any more once it was moved
  const from = await page.evaluate(() => {
    for (const sl of document.querySelectorAll('vaadin-split-layout')) {
      const r = sl.shadowRoot?.querySelector('[part="splitter"]')?.getBoundingClientRect();
      if (r && r.height > 300 && r.x > 300 && r.x < 1000 && r.y < 488 && r.y + r.height > 488) return r.x + r.width / 2;
    }
    return 405;
  });
  if (Math.abs(from - to) < 10) return;
  await page.mouse.move(from, 488);
  await page.mouse.down();
  await page.mouse.move((from + to) / 2, 488, { steps: 5 });
  await page.mouse.move(to, 488, { steps: 5 });
  await page.mouse.up();
  await idle(page, 600);
}

/** Opens a tab of the sidebar of the story editor (Outline, Author's Note, Characters, Side Query...). */
export async function sideTab(page, name) {
  await page.getByRole('tab', { name, exact: true }).first().click();
  await idle(page, 700);
}

/**
 * Opens the settings menu (the cog) below the story. Its place depends on the width of the sidebar (saved with the book),
 * so it is looked up: the last menu bar button of the bottom row.
 */
export async function openStoryMenu(page) {
  await page.locator('vaadin-horizontal-layout.border-top-subtle vaadin-menu-bar-button:not([slot="overflow"])').last().click();
  await idle(page, 500);
}

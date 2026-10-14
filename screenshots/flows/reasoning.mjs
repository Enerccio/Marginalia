// A branch from part #4 written by the reasoning model, so that a part with reasoning exists (reasoning-view.png).
import {bookTab, dialog, idle, login, main, openBook, pick, press, sleep, snap} from '../lib/shot.mjs';
import {fillTurn, generate, partAction} from '../lib/story.mjs';
import {turns} from '../dataset/story.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await partAction(page, 3, 'Branch Story');
  await idle(page, 800);
  if (await page.locator('vaadin-confirm-dialog[opened], vaadin-dialog[opened]').filter({ hasText: 'Branch' }).count() > 1) {
    await press(dialog(page), 'Yes');
  }
  await bookTab(page, 'About');
  await pick(page, 'Model', 'Local Reasoner');
  await sleep(500);
  await bookTab(page, 'Story');
  await fillTurn(page, turns.t5alt);
  await generate(page, turns.t5alt);
  await snap(page, '_t5alt');
  await bookTab(page, 'About');
  await pick(page, 'Model', 'Local LLM');
  await sleep(500);
  // back to the main branch: its end is the left card at the bottom of the tree
  await bookTab(page, 'Branch view');
  await page.mouse.move(1000, 500);
  await page.mouse.wheel(0, 700);
  await sleep(600);
  await page.mouse.click(750, 764);
  await idle(page, 1500);
  await snap(page, '_back-on-main');
});

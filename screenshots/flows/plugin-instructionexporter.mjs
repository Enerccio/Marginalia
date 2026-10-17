// Instruction Exporter: the "Export instructions" item of the settings menu (nothing changes).
import {openStoryMenu} from '../lib/story.mjs';
import {bookTab, idle, login, main, openBook, showToasts, sleep, snap} from '../lib/shot.mjs';

await main(async ({ page, context }) => {
  await context.grantPermissions(['clipboard-read', 'clipboard-write'], { origin: process.env.SHOT_URL || 'http://localhost:8099' }).catch(() => {});
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await openStoryMenu(page);
  // the place of the menu depends on the width of the sidebar (saved with the book)
  const cog = await page.locator('vaadin-horizontal-layout.border-top-subtle vaadin-menu-bar-button:not([slot="overflow"])').last().boundingBox();
  await snap(page, 'plugin-instructionexporter', { clip: { x: Math.max(0, cog.x - 134), y: 600, width: 480, height: 260 } });
  await showToasts(page);
  await page.getByText('Export instructions', { exact: true }).click();
  await sleep(1200);
  await snap(page, '_instructions-toast', { clip: { x: 0, y: 790, width: 700, height: 142 } });
  console.log('clipboard:', (await page.evaluate(() => navigator.clipboard.readText().catch(e => 'ERR ' + e))).slice(0, 200).replace(/\n/g, ' | '));
});

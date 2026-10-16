// The sidebar of the story editor with its tabs dragged apart (TabDock): Side Query under the others,
// Characters detached into a column of its own. Pictures: story-editor-tabs.
// State: a COPY of 22-arcoutline (the arrangement is saved with the book, so the data is discarded afterwards);
// Characters and Side Query come from steps 14 and 17, the Arcs tab from step 22.
import {bookTab, idle, login, main, openBook, sleep, snap} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

/** The tab with this caption of the sidebar. */
const tabOf = (page, name) => page.getByRole('tab', { name, exact: true }).first();

/** Drags a tab with the mouse (real mouse moves start the HTML5 drag) onto the center of the element `target` returns;
 *  `target` is called after the drag started, the drop strips only exist while a tab is dragged. */
async function dragTab(page, name, target) {
  const box = await tabOf(page, name).boundingBox();
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2);
  await page.mouse.down();
  await page.mouse.move(box.x + box.width / 2 + 12, box.y + box.height / 2 + 12, { steps: 4 });
  await sleep(800);                                    // the server shows the strips
  const t = await target();
  await page.mouse.move(t.x + t.width / 2, t.y + t.height / 2, { steps: 12 });
  await sleep(300);
  await page.mouse.up();
  await idle(page, 1200);
}

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Story');
  await widenSidebar(page, 900);

  // Side Query first, while it is still visible: it goes under the sheet, in a row of its own (with the six tabs
  // of the sidebar, Side Query is hidden behind the arrow of a sheet of the width that is left after the next drag)
  await dragTab(page, 'Side Query', async () => {
    const zones = page.locator('.tab-dock-zone:not(.tab-dock-side-zone)');
    const n = await zones.count();
    let best = null;
    for (let i = 0; i < n; i++) {
      const b = await zones.nth(i).boundingBox();
      if (b && b.width > 0 && (!best || (b.x > best.x - 5 && b.y > best.y))) best = b;
    }
    return best;
  });
  // Characters into a column of its own, on the right side of the sidebar
  await dragTab(page, 'Characters', async () => {
    const zones = page.locator('.tab-dock-side-zone');
    const n = await zones.count();
    return zones.nth(n - 1).boundingBox();
  });
  await sideTab(page, 'Arcs');
  await sleep(500);
  await snap(page, 'story-editor-tabs');
});

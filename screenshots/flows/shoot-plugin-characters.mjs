// Character Tracker screenshots (nothing is changed).
import {
    bookTab,
    closeDialog,
    closeRestoredBook,
    dialog,
    dialogRects,
    idle,
    launch,
    login,
    main,
    openBook,
    section,
    sleep,
    snap,
    snapDialog
} from '../lib/shot.mjs';
import {sideTab, widenSidebar} from '../lib/story.mjs';

await main(async ({ page }) => {
  await login(page);
  // every part starts from a freshly opened book
  const fresh = async () => {
    await page.reload();
    await idle(page, 1500);
    await closeRestoredBook(page);
    await openBook(page);
    await bookTab(page, 'Story');
    await widenSidebar(page, 700);
    await sideTab(page, 'Characters');
  };

  await section(page, 'tab', async () => {
    await fresh();
    // expand Draconia (the chevron of her row) to show icon and note
    const y = await page.evaluate(() => {
      const el = [...document.querySelectorAll('*')].find(e => e.children.length === 0 && e.textContent.trim() === 'Harry Potter' && e.getBoundingClientRect().width > 0 && e.getBoundingClientRect().x < 700);
      const r = el.getBoundingClientRect(); return r.y + r.height / 2;
    });
    await page.mouse.click(566, y);
    await sleep(700);
    await snap(page, 'plugin-charactertracker-tab');
  });

  await section(page, 'dialog', async () => {
    await fresh();
    const y = await page.evaluate(() => {
      const el = [...document.querySelectorAll('*')].find(e => e.children.length === 0 && e.textContent.trim() === 'Harry Potter' && e.getBoundingClientRect().width > 0 && e.getBoundingClientRect().x < 700);
      const r = el.getBoundingClientRect(); return r.y + r.height / 2;
    });
    await page.mouse.click(602, y);
    await idle(page, 900);
    const d = page.locator('vaadin-dialog[opened]').last();
    await d.getByRole('button', { name: 'Add relationship' }).scrollIntoViewIfNeeded();
    await sleep(400);
    await snapDialog(page, 'plugin-charactertracker-dialog');
    await closeDialog(page);
  });

  await section(page, 'unsaved', async () => {
    // Cancel with an edited field asks before the changes are thrown away (nothing is saved: the answer is Yes)
    await fresh();
    const y = await page.evaluate(() => {
      const el = [...document.querySelectorAll('*')].find(e => e.children.length === 0 && e.textContent.trim() === 'Harry Potter' && e.getBoundingClientRect().width > 0 && e.getBoundingClientRect().x < 700);
      const r = el.getBoundingClientRect(); return r.y + r.height / 2;
    });
    await page.mouse.click(602, y);
    await idle(page, 900);
    const d = page.locator('vaadin-dialog[opened]').last();
    await d.getByLabel('Tagline', { exact: true }).first().fill('The boy who lived, twice');
    await d.getByLabel('Tagline', { exact: true }).first().dispatchEvent('change');
    await d.getByRole('button', { name: 'Cancel', exact: true }).click();
    await idle(page, 600);
    await snapDialog(page, 'plugin-charactertracker-unsaved');
    await page.locator('vaadin-dialog[opened]').last().getByRole('button', { name: 'Yes', exact: true }).click();
    await idle(page, 500);
  });

  await section(page, 'map', async () => {
    // the map is small in the dialog: a second browser with a 2x pixel ratio gives a readable picture of the graph
    const hi = await launch({ scale: 2 });
    try {
      await login(hi.page);
      await openBook(hi.page);
      await bookTab(hi.page, 'Story');
      await widenSidebar(hi.page, 700);
      await sideTab(hi.page, 'Characters');
      await hi.page.getByRole('button', { name: 'Relationship map' }).click();
      await idle(hi.page, 1500);
      await sleep(1500);
      const [r] = await dialogRects(hi.page);
      await snap(hi.page, 'plugin-charactertracker-map', { clip: { x: r.x + 620, y: r.y + 100, width: 480, height: 550 } });
    } finally {
      await hi.browser.close();
    }
  });

  await section(page, 'import', async () => {
    await fresh();
    await page.mouse.click(294, 220);                           // the wand
    await sleep(500);
    await page.getByText('Import lore entries as characters', { exact: true }).click();
    await idle(page, 900);
    await snapDialog(page, 'plugin-charactertracker-import');
    await closeDialog(page);
  });
});

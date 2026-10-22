// Lore Generator: a new entry for the Three Broomsticks - prompt, generation, compaction, copy to the entry, Tools.
import {bookTab, closeDialog, fill, idle, llm, login, main, openBook, press, sleep, snapDialog} from '../lib/shot.mjs';

await main(async ({ page }) => {
  await login(page);
  await openBook(page);
  await bookTab(page, 'Lorebook');
  await press(page, 'Add Entry', { exact: false });
  await idle(page, 1000);
  await page.evaluate(() => { for (const g of document.querySelectorAll('vaadin-grid')) g.scrollToIndex(g.size - 1); });
  await sleep(1000);
  const row = await page.evaluate(() => {
    const f = [...document.querySelectorAll('vaadin-text-field')].filter(x => x.value === 'New Entry').pop();
    const r = f.getBoundingClientRect(); return { y: r.y + r.height / 2 };
  });
  await page.mouse.click(400, row.y);
  await page.keyboard.press('Meta+A');
  await page.keyboard.type('The Three Broomsticks');
  await page.keyboard.press('Tab');
  await sleep(800);
  await page.mouse.click(1845, row.y);                         // the wand of the row
  await idle(page, 1200);

  const d = page.locator('vaadin-dialog[opened]').last();
  await fill(d, 'Prompt', 'Write the lore entry for "The Three Broomsticks", the inn of Hogsmeade, about 120 words as one paragraph: Madam Rosmerta, the dark wooden room, butterbeer, the Aurors at the door in autumn 1996 and the lane behind the inn.');
  await press(d, 'Run Generation');
  await page.getByText('Madam Rosmerta', { exact: false }).last().waitFor({ timeout: 30000 });
  await page.getByRole('button', { name: 'Stop', exact: true }).waitFor({ state: 'detached', timeout: 20000 }).catch(() => {});
  await sleep(1000);
  await snapDialog(page, 'plugin-loregenerator', -1);

  await d.getByRole('tab', { name: 'Compaction' }).click();
  await sleep(500);
  await press(d, 'Run Compaction');
  await page.getByText('Three Broomsticks: Hogsmeade inn', { exact: false }).last().waitFor({ timeout: 30000 });
  await page.getByRole('button', { name: 'Stop', exact: true }).waitFor({ state: 'detached', timeout: 20000 }).catch(() => {});
  await sleep(1000);
  await snapDialog(page, 'plugin-loregenerator-compaction', -1);

  // the Tools dialog (needs the Name generator, Name provider and Place provider extensions): names of Japan from both
  // sources (the dialog with the first one open), then the places on with the hint opened (the other two closed)
  await press(d, 'Tools', { exact: false });
  await idle(page, 1500);
  const tools = page.locator('vaadin-dialog[opened]').last();
  const section = name => tools.locator('vaadin-details').filter({ has: page.locator(`vaadin-details-summary:has-text("${name}")`) }).first();
  const open = async name => {
    const sec = section(name);
    if ((await sec.locator('vaadin-details-summary').first().getAttribute('aria-expanded')) !== 'true') {
      await tools.getByText(name, { exact: true }).first().click();
    }
    await sleep(500);
    return sec;
  };
  // the checkbox of the row with this text (the grid reuses its row elements, so the row is found by its text)
  const check = async (sec, text) => {
    const cell = await sec.locator('vaadin-grid-cell-content').filter({ hasText: text }).first().boundingBox();
    // the "select all" checkbox of the header is hidden: the first one that has a box is the one of a row
    let box = null;
    const boxes = sec.locator('vaadin-grid vaadin-checkbox');
    for (let i = 0; i < await boxes.count() && !box; i++) box = await boxes.nth(i).boundingBox();
    await page.mouse.click(box.x + box.width / 2, cell.y + cell.height / 2);
    await sleep(300);
  };
  for (const name of ['Name generator', 'Name provider']) {
    const sec = await open(name);
    await sec.locator('input[placeholder="Filter"]').fill('Japan');
    await sleep(1200);
    await check(sec, 'Japan - Surnames');
    if (name === 'Name generator') await snapDialog(page, 'tools-dialog');
    await tools.getByText(new RegExp('^' + name + ' \\(1\\)$')).first().click();   // close it: the next section comes into view
    await sleep(500);
  }
  const places = await open('Place provider');
  await places.locator('vaadin-checkbox').first().click();
  await sleep(500);
  await tools.getByText('Hint for the model', { exact: true }).last().click();
  await sleep(700);
  await snapDialog(page, 'tools-hint');
  await press(tools, 'Save');
  await sleep(800);

  await press(d, 'Copy to Lore Entry', { exact: false });
  await idle(page, 1000);
  console.log(JSON.stringify((await llm('/_log')).slice(-2).map(r => ({ sys: r.messages[0].content.slice(0, 200), last: r.messages.at(-1).content.slice(0, 300) }))));
});

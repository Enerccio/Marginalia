// Lore Generator: a new entry for the Three Broomsticks - prompt, generation, compaction, copy to the entry, Name tools.
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

  // the Name tools (needs the Name generator and the Name provider extensions)
  await press(d, 'Name tools', { exact: false });
  await idle(page, 1500);
  const names = page.locator('vaadin-dialog[opened]').last();
  // one category of each source: the filter "Japan" lists the ones of the Name generator first, the ones of the Name
  // provider last (the first checkbox is the one of the header)
  await names.locator('input[placeholder="Filter categories"]').fill('Japan');
  await sleep(1200);
  // the checkbox of the row with this category (the grid reuses its row elements, so the row is found by its text)
  const check = async text => {
    const cell = await names.locator('vaadin-grid-cell-content').filter({ hasText: text }).first().boundingBox();
    const box = await names.locator('vaadin-checkbox').nth(1).boundingBox();
    await page.mouse.click(box.x + box.width / 2, cell.y + cell.height / 2);
    await sleep(300);
  };
  await check('Generator: Japan - Surnames');
  await check('Provider: Japan - Surnames');
  await sleep(500);
  await snapDialog(page, 'plugin-loregenerator-names');
  await closeDialog(page);

  await press(d, 'Copy to Lore Entry', { exact: false });
  await idle(page, 1000);
  console.log(JSON.stringify((await llm('/_log')).slice(-2).map(r => ({ sys: r.messages[0].content.slice(0, 200), last: r.messages.at(-1).content.slice(0, 300) }))));
});
